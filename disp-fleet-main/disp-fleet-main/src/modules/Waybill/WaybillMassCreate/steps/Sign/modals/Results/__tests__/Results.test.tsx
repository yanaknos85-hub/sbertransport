import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { act } from 'react-dom/test-utils';
import { mockMassCreateFirstTitle, mockShifts, mockEwbShiftResponses } from 'modules/Waybill/WaybillMassCreate/__tests__/__mocks__/apiMocks';
import Results from '../Results';

// Mock dependencies
const mockUseTranslation = jest.fn();
const mockUseWebsocket = jest.fn();
const mockUseAppStore = jest.fn();

// Мок для useAppStore должен быть установлен ДО импорта Results
jest.mock('ioc', () => ({
  useAppStore: () => mockUseAppStore(),
  AppStoreContext: {
    Provider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  },
}));

jest.mock('i18n', () => ({
  useTranslation: () => mockUseTranslation(),
}));

// Мок для api/websocket должен быть после мока useAppStore
jest.mock('api/websocket', () => ({
  useWebsocket: () => mockUseWebsocket(),
}));

describe('Results', () => {
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseTranslation.mockReturnValue({
      t: {
        Waybill: {
          modal: {
            resultsTitleCreate: 'Результат создания ЭПЛ',
            okText: 'Закрыть',
          },
        },
      },
    });
    mockUseAppStore.mockReturnValue({
      logger: {
        toNotify: jest.fn(),
        toMessage: jest.fn(),
      },
      process: {
        decodeResponseData: jest.fn((data: { data: unknown }) => data.data),
        getResponseData: jest.fn((res: { data: unknown }) => res.data),
      },
      authStore: {
        checkAuth: jest.fn(() => Promise.resolve({ headers: { Authorization: 'Bearer test-token' } })),
      },
    });
  });

  it('должен отображать ошибки в таблице', async () => {
    const firstTitleResult = [
      mockMassCreateFirstTitle[0],
      mockMassCreateFirstTitle[1],
      mockMassCreateFirstTitle[2],
    ];

    // Подготавливаем данные для рендеринга - симулируем получение сообщений через useEffect
    const mockResults = mockEwbShiftResponses.slice(0, 3);

    // Мокаем useWebsocket с начальным значением null
    mockUseWebsocket.mockReturnValue({ lastMessage: null });

    const { rerender } = render(
      <Results
        visible={true}
        firstTitleResult={firstTitleResult}
        selectedShifts={mockShifts}
        onClose={mockOnClose}
      />
    );

    expect(await screen.findByRole('dialog')).toBeInTheDocument();

    // Симулируем получение сообщений по одному через useEffect
    await act(async () => {
      // Имитируем приход первого сообщения
      mockUseWebsocket.mockReturnValue({ lastMessage: { data: mockResults[0] } });
      rerender(
        <Results
          visible={true}
          firstTitleResult={firstTitleResult}
          selectedShifts={mockShifts}
          onClose={mockOnClose}
        />
      );
    });

    await act(async () => {
      // Имитируем приход второго сообщения
      mockUseWebsocket.mockReturnValue({ lastMessage: { data: mockResults[1] } });
      rerender(
        <Results
          visible={true}
          firstTitleResult={firstTitleResult}
          selectedShifts={mockShifts}
          onClose={mockOnClose}
        />
      );
    });

    await act(async () => {
      // Имитируем приход третьего сообщения
      mockUseWebsocket.mockReturnValue({ lastMessage: { data: mockResults[2] } });
      rerender(
        <Results
          visible={true}
          firstTitleResult={firstTitleResult}
          selectedShifts={mockShifts}
          onClose={mockOnClose}
        />
      );
    });

    // Даем время на ререндер после всех обновлений
    await waitFor(() => {
      expect(screen.getByText(/Создано ЭПЛ: 2 из 3/)).toBeInTheDocument();
    }, { timeout: 1000 });

    expect(screen.getByText('Госномер')).toBeInTheDocument();
    expect(screen.getByText('Ошибка')).toBeInTheDocument();
    expect(screen.getByText('Ошибка подписания документа')).toBeInTheDocument();
    expect(screen.getByText('М789НТ199')).toBeInTheDocument();
  });
});
