import { useState, useCallback, useRef } from 'react';
import { encodeWav } from 'utils/encodeWav';

const CHAT_MAX_RECORDING_DURATION = 60; // seconds
const AUDIO_LEVEL_FRAMES = 16; // number of bars in indicator
const SMOOTHING = 0.3; // smoothing factor

interface UseRecordingReturn {
  isRecording: boolean;
  isProcessingAudio: boolean;
  audioDuration: number;
  /** Array of audio levels 0..1 for each bar (16 values) */
  audioLevels: number[];
  /** Start recording. Calls callback with final WAV Blob after stop */
  startRecording: (onAudioReady: (audioBlob: Blob) => Promise<void>) => Promise<void>;
  /** Stop recording */
  stopRecording: () => void;
}

/**
 * Converts raw time-domain data to array of levels 0..1 for visualizer.
 */
const computeLevels = (
  dataArray: Uint8Array,
  barCount: number
): number[] => {
  const sampleRate = dataArray.length;
  const chunkSize = Math.floor(sampleRate / barCount);
  const levels: number[] = [];

  for (let i = 0; i < barCount; i++) {
    const start = i * chunkSize;
    const end = i === barCount - 1 ? sampleRate : start + chunkSize;

    let sumSquares = 0;
    for (let j = start; j < end; j++) {
      const normalized = (dataArray[j] - 128) / 128;
      sumSquares += normalized * normalized;
    }
    const rms = Math.sqrt(sumSquares / (end - start));
    levels.push(Math.min(1, rms * 2.5));
  }

  return levels;
};

/**
 * Smooth one array to another (exponential)
 */
const smoothLevels = (
  current: number[],
  target: number[],
  factor: number
): number[] => current.map((val, i) => val + (target[i] - val) * (1 - factor));

/**
 * Hook for voice recording from microphone.
 * Captures raw PCM via ScriptProcessorNode and converts to WAV on stop.
 */
const useRecording = (): UseRecordingReturn => {
  const [isRecording, setIsRecording] = useState(false);
  const [isProcessingAudio, setIsProcessingAudio] = useState(false);
  const [audioDuration, setAudioDuration] = useState(0);
  const [audioLevels, setAudioLevels] = useState<number[]>(
    Array.from({ length: AUDIO_LEVEL_FRAMES }, () => 0)
  );

  const timerIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const onAudioReadyRef = useRef<((blob: Blob) => Promise<void>) | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const rafRef = useRef<number>(0);
  const prevLevelsRef = useRef<number[]>(
    Array.from({ length: AUDIO_LEVEL_FRAMES }, () => 0)
  );
  const scriptProcessorRef = useRef<ScriptProcessorNode | null>(null);
  const pcmBufferRef = useRef<Float32Array>(new Float32Array(0));
  const pcmCursorRef = useRef<number>(0);

  const clearTimer = useCallback(() => {
    if (timerIntervalRef.current) {
      clearInterval(timerIntervalRef.current);
      timerIntervalRef.current = null;
    }
  }, []);

  const stopAudioAnalysis = useCallback(() => {
    if (rafRef.current) {
      cancelAnimationFrame(rafRef.current);
      rafRef.current = 0;
    }
    if (audioContextRef.current && audioContextRef.current.state !== 'closed') {
      audioContextRef.current.close();
    }
    audioContextRef.current = null;
    analyserRef.current = null;
  }, []);

  const stopMediaTracks = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
  }, []);

  const finishRecording = useCallback(() => {
    clearTimer();
    stopAudioAnalysis();
    stopMediaTracks();

    if (scriptProcessorRef.current) {
      scriptProcessorRef.current.disconnect();
      scriptProcessorRef.current = null;
    }
  }, [clearTimer, stopAudioAnalysis, stopMediaTracks]);

  const startRecording = useCallback(
    async (onAudioReady: (audioBlob: Blob) => Promise<void>) => {
      if (isRecording || isProcessingAudio) return;

      onAudioReadyRef.current = onAudioReady;

      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });
        streamRef.current = stream;

        const audioContext = new AudioContext();
        const source = audioContext.createMediaStreamSource(stream);
        const analyser = audioContext.createAnalyser();
        analyser.fftSize = 256;
        source.connect(analyser);
        audioContextRef.current = audioContext;
        analyserRef.current = analyser;

        // --- Audio level visualizer loop ---
        const dataArray = new Uint8Array(analyser.frequencyBinCount);
        prevLevelsRef.current = Array.from(
          { length: AUDIO_LEVEL_FRAMES },
          () => 0
        );

        const analyzeLoop = () => {
          analyser.getByteTimeDomainData(dataArray);
          const rawLevels = computeLevels(dataArray, AUDIO_LEVEL_FRAMES);
          const smoothed = smoothLevels(
            prevLevelsRef.current,
            rawLevels,
            SMOOTHING
          );
          prevLevelsRef.current = smoothed;
          setAudioLevels(smoothed);
          rafRef.current = requestAnimationFrame(analyzeLoop);
        };
        analyzeLoop();

        // --- PCM capture via ScriptProcessorNode ---
        const sampleRate = audioContext.sampleRate;
        const maxSamples = sampleRate * (CHAT_MAX_RECORDING_DURATION + 1);
        pcmBufferRef.current = new Float32Array(maxSamples);
        pcmCursorRef.current = 0;

        const scriptProcessor = audioContext.createScriptProcessor(
          4096,
          1,
          1
        );
        scriptProcessor.onaudioprocess = (
          event: AudioProcessingEvent
        ) => {
          const channelData = event.inputBuffer.getChannelData(0);
          const length = channelData.length;
          const cursor = pcmCursorRef.current;

          if (cursor + length <= maxSamples) {
            pcmBufferRef.current.set(channelData, cursor);
            pcmCursorRef.current = cursor + length;
          }
        };

        source.connect(scriptProcessor);
        scriptProcessor.connect(audioContext.destination);
        scriptProcessorRef.current = scriptProcessor;

        setIsRecording(true);
        setAudioDuration(0);
        setAudioLevels(
          Array.from({ length: AUDIO_LEVEL_FRAMES }, () => 0)
        );

        const startTime = Date.now();
        timerIntervalRef.current = setInterval(() => {
          const elapsed = Math.floor((Date.now() - startTime) / 1000);
          setAudioDuration(elapsed);

          if (elapsed >= CHAT_MAX_RECORDING_DURATION) {
            finishRecording();
          }
        }, 1000);
      } catch (err) {
        stopMediaTracks();
        throw err;
      }
    },
    [isRecording, isProcessingAudio, finishRecording, stopMediaTracks]
  );

  const stopRecording = useCallback(async () => {
    finishRecording();

    const sampleBuffer = pcmBufferRef.current.slice(
      0,
      pcmCursorRef.current
    );

    if (sampleBuffer.length === 0) {
      setIsRecording(false);
      return;
    }

    const sampleRate
      = audioContextRef.current?.sampleRate ?? 48000;
    const wavBlob = encodeWav(sampleBuffer, sampleRate);

    setIsRecording(false);
    setIsProcessingAudio(true);

    const cb = onAudioReadyRef.current;
    onAudioReadyRef.current = null;

    if (cb) {
      try {
        await cb(wavBlob);
      } finally {
        setIsProcessingAudio(false);
      }
    } else {
      setIsProcessingAudio(false);
    }
  }, [finishRecording]);

  return {
    isRecording,
    isProcessingAudio,
    audioDuration,
    audioLevels,
    startRecording,
    stopRecording,
  };
};

export default useRecording;
