import React, {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useRef,
  useState
} from 'react';
import cn from 'classnames';

import { useRecognizeAudioMessage } from 'api/smart-speeach-integration/smart-speeach-integration.api';
import { TAudioMessageResponse } from 'api/smart-speeach-integration/smart-speeach-integration.types';
import MessageRow from './components/MessageRow';
import TypingIndicator from './components/TypingIndicator';
import InputToolbar from './components/InputToolbar';
import useRecording from './hooks/useRecording';
import useChatMessages from './hooks/useChatMessages';
import { useAutoScroll } from './hooks/useAutoScroll';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import styles from './Chat.module.scss';
import ErrorBoundary from 'shared/components/ErrorBoundary';

export interface ChatRef {
  newChat: () => void;
}

interface ChatProps {
  onHasMessages?: (hasMessages: boolean) => void;
}

const Chat = forwardRef<ChatRef, ChatProps>(({ onHasMessages }, ref) => {
  const { logger } = useAppStoreContext();
  const [inputValue, setInputValue] = useState('');
  const messagesRef = useRef<HTMLDivElement>(null);

  const {
    messages,
    isLoading,
    sendMessage,
    clearMessages,
  } = useChatMessages();

  useImperativeHandle(ref, () => ({
    newChat: clearMessages,
  }), [clearMessages]);

  const {
    isRecording,
    isProcessingAudio,
    audioDuration,
    audioLevels,
    startRecording,
    stopRecording,
  } = useRecording();

  const [recognizeAudioMessage] = useRecognizeAudioMessage();

  const handleSend = useCallback((textArg?: string) => {
    const text = (textArg ?? inputValue).trim();
    if (!text || isLoading) return;

    sendMessage(text);
    setTimeout(() => setInputValue(''));
  }, [inputValue, isLoading, sendMessage]);

  const handleStartRecording = useCallback(async () => {
    try {
      await startRecording(async (audioBlob: Blob) => {
        try {
          const audioData: TAudioMessageResponse = await recognizeAudioMessage(audioBlob);
          const recognizedText = audioData?.message?.recognizedText || '';

          if (recognizedText) {
            handleSend(recognizedText);
          }
        } catch (error) {
          logger.toMessage(
            'error',
            (error as any).response?.data?.code === 'AUDIO_RECOGNITION_FAILED'
              ? 'Речь не распознана'
              : (error as any).response?.data?.message || (error as Error).message || 'Ошибка распознавания речи'
          );
        }
      });
    } catch (error) {
      logger.toMessage(
        'error',
        (error as any).response?.data?.message || (error as Error).message || 'Ошибка доступа к микрофону'
      );
    }
  }, [startRecording, recognizeAudioMessage, handleSend, logger]);

  useAutoScroll(messagesRef, [messages]);

  useEffect(() => {
    onHasMessages?.(messages.length > 0);
  }, [messages, onHasMessages]);

  return (
    <div className={styles.chat}>
      <div
        className={cn(styles.messages, { [styles.messagesFill]: messages.length > 0 })}
        ref={messagesRef}
      >
        {messages.map(msg => (
          <ErrorBoundary key={msg.id}>
            <MessageRow
              id={msg.id}
              text={msg.text}
              isUser={msg.isUser}
              draft={msg.draft}
            />
          </ErrorBoundary>
        ))}
        {isLoading && <TypingIndicator />}
        <div />
      </div>

      <InputToolbar
        inputValue={inputValue}
        isRecording={isRecording}
        isProcessingAudio={isProcessingAudio}
        audioDuration={audioDuration}
        audioLevels={audioLevels}
        isLoading={isLoading}
        onChange={setInputValue}
        onSend={() => handleSend()}
        onStartRecording={handleStartRecording}
        onStopRecording={stopRecording}
      />
    </div>
  );
});

export default Chat;
