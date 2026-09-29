import { useCallback, useRef, useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useSendDraftPilotMessage } from 'api/draft-pilot/draft-pilot.api';
import type { TUserMessageResponse, TDraftDto, TUserMessageRequest } from 'api/draft-pilot/draft-pilot.types';
import { UUID } from 'utils/io-ts';

interface ChatMessage {
  id: string;
  text: string;
  isUser: boolean;
  draft: TDraftDto | null;
}

interface UseChatMessagesReturn {
  /** Массив сообщений */
  messages: ChatMessage[];
  /** Идёт ли отправка сообщения */
  isLoading: boolean;
  /** Идентификатор сессии */
  sessionId: string | null;
  /** Отправить текстовое сообщение */
  sendMessage: (text: string) => Promise<void>;
  /** Сбросить сообщения. Ответы уже отправленных, но ещё не обработанных запросов игнорируются */
  clearMessages: () => void;
}

/**
 * Хук для управления списком сообщений чата.
 *
 * - Добавляет пользовательское сообщение в список
 * - Вызывает `sendMessage` / `sendAudioMessage`
 * - Обрабатывает ответ ассистента (добавляет в список)
 * - При вызове `clearMessages` сбрасывает локальное состояние и помечает все in-flight запросы
 *   как устаревшие: их ответы и ошибки игнорируются и не отображаются в новом чате.
 */
const useChatMessages = (): UseChatMessagesReturn => {
  const { logger } = useAppStoreContext();

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isLocalLoading, setIsLocalLoading] = useState(false);
  const [sendMessageApi] = useSendDraftPilotMessage();

  const requestIdRef = useRef(0);

  const sendMessage = useCallback(
    async (text: string): Promise<void> => {
      const requestId = ++requestIdRef.current;

      const userMessage: ChatMessage = {
        id: `user-${Date.now()}`,
        text,
        isUser: true,
        draft: null,
      };

      setMessages(prev => [...prev, userMessage]);
      setIsLocalLoading(true);

      const payload: TUserMessageRequest = {
        text,
        sessionId: sessionId as UUID | null,
      };

      try {
        const data: TUserMessageResponse = await sendMessageApi(payload);

        if (requestId !== requestIdRef.current) {
          return;
        }

        if (data.data?.session?.sessionId) {
          setSessionId(data.data.session.sessionId);
        }

        const replyText = data.message?.text || '';

        const assistantMessage: ChatMessage = {
          id: `ai-${Date.now()}`,
          text: replyText,
          isUser: false,
          draft: data.data?.draft ?? null,
        };

        setMessages(prev => [...prev, assistantMessage]);
      } catch (error) {
        if (requestId !== requestIdRef.current) {
          return;
        }

        logger.toMessage(
          'error',
          (error as any).response?.data?.message || (error as Error).message || 'Произошла ошибка'
        );
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLocalLoading(false);
        }
      }
    },
    [sessionId, sendMessageApi, logger]
  );

  const clearMessages = useCallback(() => {
    // Инкрементируем идентификатор текущего запроса — все in-flight ответы
    // с устаревшим requestId будут проигнорированы и не отобразятся в новом чате.
    requestIdRef.current += 1;
    setMessages([]);
    setSessionId(null);
    setIsLocalLoading(false);
  }, []);

  return {
    messages,
    isLoading: isLocalLoading,
    sessionId,
    sendMessage,
    clearMessages,
  };
};

export default useChatMessages;
