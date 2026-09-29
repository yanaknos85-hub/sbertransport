import { act, renderHook } from '@testing-library/react-hooks';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useSendDraftPilotMessage } from 'api/draft-pilot/draft-pilot.api';
import type { TUserMessageResponse } from 'api/draft-pilot/draft-pilot.types';
import { MessageType } from 'api/draft-pilot/draft-pilot.constants';

import useChatMessages from '../useChatMessages';

jest.mock('shared/hooks/useEmpContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('api/draft-pilot/draft-pilot.api', () => ({
  useSendDraftPilotMessage: jest.fn(),
}));

const mockUseAppStoreContext = useAppStoreContext as jest.Mock;
const mockUseSendDraftPilotMessage = useSendDraftPilotMessage as jest.Mock;

const mockLogger = {
  toMessage: jest.fn(),
};

const createMockResponse = (overrides: Partial<TUserMessageResponse> = {}): TUserMessageResponse => ({
  data: {
    traceId: '00000000-0000-0000-0000-000000000000',
    session: {
      sessionId: '11111111-1111-1111-1111-111111111111',
      status: 'ACTIVE',
    },
    draft: null,
  },
  message: {
    type: MessageType.INFO,
    text: 'Ответ ассистента',
  },
  action: 'COMPLETED',
  ...overrides,
} as TUserMessageResponse);

describe('useChatMessages', () => {
  let mockMutate: jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();
    mockMutate = jest.fn();

    mockUseAppStoreContext.mockReturnValue({ logger: mockLogger });

    // По умолчанию имитируем успешный ответ
    mockMutate.mockImplementation(() => Promise.resolve(createMockResponse()));

    mockUseSendDraftPilotMessage.mockReturnValue([mockMutate, { isLoading: false }]);
  });

  it('должен добавлять пользовательское сообщение и ответ ассистента', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useChatMessages());

    act(() => {
      result.current.sendMessage('Привет');
    });

    expect(result.current.messages).toHaveLength(1);
    expect(result.current.messages[0]).toMatchObject({
      text: 'Привет',
      isUser: true,
    });

    await waitForNextUpdate();

    expect(result.current.messages).toHaveLength(2);
    expect(result.current.messages[1]).toMatchObject({
      text: 'Ответ ассистента',
      isUser: false,
    });
    expect(result.current.sessionId).toBe('11111111-1111-1111-1111-111111111111');
  });

  it('clearMessages должен сбрасывать сообщения и sessionId', async () => {
    const { result, waitForNextUpdate } = renderHook(() => useChatMessages());

    act(() => {
      result.current.sendMessage('Привет');
    });

    await waitForNextUpdate();

    expect(result.current.messages).toHaveLength(2);
    expect(result.current.sessionId).not.toBeNull();

    act(() => {
      result.current.clearMessages();
    });

    expect(result.current.messages).toHaveLength(0);
    expect(result.current.sessionId).toBeNull();
  });

  describe('TRANSPORT-44929: результат предыдущего запроса не должен отображаться в новом чате', () => {
    it('должен скрывать индикатор загрузки сразу после clearMessages', async () => {
      let resolveRequest: (value: TUserMessageResponse) => void = () => {};
      mockMutate.mockImplementation(() => new Promise<TUserMessageResponse>((resolve) => {
        resolveRequest = resolve;
      }));

      const { result } = renderHook(() => useChatMessages());

      act(() => {
        result.current.sendMessage('Закажи такси на сегодня в 12 часов');
      });

      // К моменту отправки пользователь видит своё сообщение и индикатор загрузки
      expect(result.current.messages).toHaveLength(1);
      expect(result.current.isLoading).toBe(true);

      // Имитируем клик на "Новый чат" до получения ответа
      act(() => {
        result.current.clearMessages();
      });

      // Сообщения и сессия сброшены
      expect(result.current.messages).toHaveLength(0);
      expect(result.current.sessionId).toBeNull();
      // Индикатор загрузки сразу пропадает
      expect(result.current.isLoading).toBe(false);

      // Резолвим отменённый запрос — он не должен повлиять на состояние чата
      await act(async () => {
        resolveRequest(createMockResponse({
          message: { type: MessageType.INFO, text: 'Ваше сообщение непонятно. Давайте начнём сначала.' },
        }));
      });

      expect(result.current.messages).toHaveLength(0);
      expect(result.current.sessionId).toBeNull();
      expect(result.current.isLoading).toBe(false);
    });

    it('должен игнорировать ответ предыдущего запроса с устаревшими данными', async () => {
      // Имитируем ситуацию, когда ответ приходит уже после clearMessages
      let resolveRequest: (value: TUserMessageResponse) => void = () => {};
      mockMutate.mockImplementation(() => new Promise<TUserMessageResponse>((resolve) => {
        resolveRequest = resolve;
      }));

      const { result } = renderHook(() => useChatMessages());

      act(() => {
        result.current.sendMessage('Закажи такси на сегодня в 12 часов');
      });

      // Открываем новый чат до ответа
      act(() => {
        result.current.clearMessages();
      });

      // Ответ предыдущего запроса приходит уже после clearMessages
      await act(async () => {
        resolveRequest(createMockResponse({
          message: { type: MessageType.INFO, text: 'Заготовка заявки с прошлыми данными' },
          data: {
            traceId: '00000000-0000-0000-0000-000000000000',
            session: {
              sessionId: '22222222-2222-2222-2222-222222222222',
              status: 'ACTIVE',
            },
            draft: { pickupTime: '2026-08-07T12:00:00.000Z' },
          } as any,
        }));
      });

      // Сообщения и сессия не должны быть перезаписаны устаревшим ответом
      expect(result.current.messages).toHaveLength(0);
      expect(result.current.sessionId).toBeNull();
    });

    it('должен игнорировать ошибку предыдущего запроса, если она пришла после clearMessages', async () => {
      let rejectRequest: (error: unknown) => void = () => {};
      mockMutate.mockImplementation(() => new Promise<TUserMessageResponse>((_, reject) => {
        rejectRequest = reject;
      }));

      const { result } = renderHook(() => useChatMessages());

      act(() => {
        result.current.sendMessage('Закажи такси');
      });

      act(() => {
        result.current.clearMessages();
      });

      await act(async () => {
        rejectRequest(new Error('boom'));
      });

      // Ошибка отменённого запроса не должна логироваться
      expect(mockLogger.toMessage).not.toHaveBeenCalled();
      expect(result.current.messages).toHaveLength(0);
      expect(result.current.isLoading).toBe(false);
    });

    it('должен обрабатывать новые сообщения корректно после clearMessages', async () => {
      const { result, waitForNextUpdate } = renderHook(() => useChatMessages());

      // Отправляем первое сообщение и сразу открываем новый чат
      mockMutate.mockImplementationOnce(() => new Promise(() => {}));
      act(() => {
        result.current.sendMessage('Первое сообщение');
      });

      act(() => {
        result.current.clearMessages();
      });

      // После clearMessages возвращаем обычное поведение имита
      mockMutate.mockImplementation(() => Promise.resolve(createMockResponse({
        message: { type: MessageType.INFO, text: 'Ответ на второе сообщение' },
      })));

      act(() => {
        result.current.sendMessage('Второе сообщение');
      });

      await waitForNextUpdate();

      // В чате только сообщения новой сессии
      expect(result.current.messages).toHaveLength(2);
      expect(result.current.messages[0]).toMatchObject({ text: 'Второе сообщение', isUser: true });
      expect(result.current.messages[1]).toMatchObject({ text: 'Ответ на второе сообщение', isUser: false });
    });
  });
});
