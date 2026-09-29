/**
 * Writes a WAV file header + PCM data into an ArrayBuffer.
 *
 * @param samples - PCM samples as Float32Array (range -1..1)
 * @param sampleRate - sample rate (e.g. 48000, 44100, 16000)
 * @returns Blob with `audio/wav` MIME type
 *
 * The function:
 * 1. Converts Float32 samples to 16-bit signed integers (Int16Array)
 * 2. Builds a standard RIFF/WAV container
 * 3. Returns a Blob ready for upload or playback
 */
export function encodeWav(
  samples: Float32Array,
  sampleRate: number
): Blob {
  const numChannels = 1; // mono
  const bitsPerSample = 16;
  const byteRate = sampleRate * numChannels * (bitsPerSample / 8);
  const blockAlign = numChannels * (bitsPerSample / 8);

  // Convert Float32 (-1..1) to Int16
  const pcmData = new Int16Array(samples.length);
  for (let i = 0; i < samples.length; i++) {
    const s = Math.max(-1, Math.min(1, samples[i]));
    pcmData[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
  }

  const dataSize = pcmData.byteLength;
  const headerSize = 44;
  const totalSize = headerSize + dataSize;

  const buffer = new ArrayBuffer(totalSize);
  const view = new DataView(buffer);

  // --- RIFF header ---
  writeString(view, 0, 'RIFF');
  view.setUint32(4, totalSize - 8, true); // file size - 8
  writeString(view, 8, 'WAVE');

  // --- fmt chunk ---
  writeString(view, 12, 'fmt ');
  view.setUint32(16, 16, true); // chunk size
  view.setUint16(20, 1, true); // PCM (1)
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, byteRate, true);
  view.setUint16(32, blockAlign, true);
  view.setUint16(34, bitsPerSample, true);

  // --- data chunk ---
  writeString(view, 36, 'data');
  view.setUint32(40, dataSize, true);

  // write PCM data
  const pcmView = new Int16Array(buffer, headerSize, pcmData.length);
  pcmView.set(pcmData);

  return new Blob([buffer], { type: 'audio/wav' });
}

/** Helper to write ASCII string into DataView */
function writeString(view: DataView, offset: number, str: string): void {
  for (let i = 0; i < str.length; i++) {
    view.setUint8(offset + i, str.charCodeAt(i));
  }
}
