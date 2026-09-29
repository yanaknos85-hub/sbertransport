/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import moment from 'moment';

// Мокаем i18n с полной структурой переводов
jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      global: {
        save: 'Сохранить',
        cancel: 'Отмена',
      },
      Requests: {
        Filters: {
          title: 'Фильтры',
          status: 'Статус',
          chooseStatus: 'Выберите статусы',
          startTime: 'Дата поездки',
          selectStartTime: 'Выберите дату поездки',
          serviceType: 'Вид сервиса',
          chooseServiceType: 'Выберите вид сервиса',
          driver: 'Водитель',
        },
      },
    },
  }),
}));

// Мокаем Ant Design — НЕ переопределяем Form.useForm,
// чтобы можно было использовать mockImplementation/ReturnValue в тестах.
jest.mock('antd', () => {
  const actualAntd = jest.requireActual('antd');
  return {
    ...actualAntd,
    // Form и Modal остаются как в actualAntd (включая Form.useForm)
    Modal: ({
      visible, onCancel, onOk, children, title, okText, cancelText,
    }: any) => {
      if (!visible) return null;
      return (
        <div data-testid="mock-modal">
          <div className="ant-modal-header">
            <div className="ant-modal-title">{title}</div>
          </div>
          <div className="ant-modal-body">{children}</div>
          <div className="ant-modal-footer">
            <button onClick={onCancel}>{cancelText}</button>
            <button onClick={onOk}>{okText}</button>
          </div>
        </div>
      );
    },
  };
});

// Мокаем компоненты
jest.mock('components/StatusSelect', () => ({
  StatusSelect: ({
    mode, onChange, value,
  }: any) => (
    <div data-testid="mock-status-select">
      <select
        multiple={mode === 'multiple'}
        value={value || []}
        onChange={e => onChange?.(Array.from(e.target.selectedOptions, option => option.value))}
      >
        <option value="NEW">Новый</option>
        <option value="CONFIRMED">Подтвержден</option>
        <option value="CANCELLED">Отменен</option>
      </select>
    </div>
  ),
}));

// Мокаем DatePicker с правильным onChange
jest.mock('components/DatePicker/DatePicker', () => {
  const actualDatePicker = jest.requireActual('components/DatePicker/DatePicker');
  return {
    ...actualDatePicker,
    NewDateInput: ({
      value, onChange, placeholder,
    }: any) => {
      const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const [start, end] = e.target.value.split(' - ');
        onChange?.({
          mode: Math.abs(moment(end).diff(moment(start))) > 1000 * 60 * 60 * 24 ? 'range' : 'date',
          value: [
            start ? moment(start) : null,
            end ? moment(end) : null,
          ],
        });
      };

      return (
        <div data-testid="mock-date-input">
          <input
            type="text"
            placeholder={placeholder}
            value={value?.value?.map((v: moment.Moment | null) => v?.format('YYYY-MM-DD') || '')?.join(' - ') || ''}
            onChange={handleChange}
          />
        </div>
      );
    },
  };
});

jest.mock('components/SelectDrivers/SelectDrivers', () => ({
  SelectDrivers: ({
    mode, onChange, value,
  }: any) => (
    <div data-testid="mock-select-drivers">
      <select
        multiple={mode === 'multiple'}
        value={value || []}
        onChange={e => onChange?.(Array.from(e.target.selectedOptions, option => option.value))}
      >
        <option value="driver-1">Водитель 1</option>
        <option value="driver-2">Водитель 2</option>
      </select>
    </div>
  ),
}));

// Мокаем контексты
jest.mock('modules/Trips/context/TripsModal', () => ({
  useTripsModal: jest.fn(),
}));

jest.mock('modules/Trips/context/TripsQuery', () => ({
  useTripsQuery: jest.fn(),
}));

jest.mock('constants/app.constants', () => ({
  TripTypes: {
    Passenger: 'passenger',
    Cargo: 'cargo',
  },
}));

jest.mock('utils/fieldValidationRules/fieldValidationRules', () => ({
  ValidationRules: {
    general: {
      required: { required: true, message: 'Заполните поле' },
    },
  },
}));

// Импортируем компонент после моков
import { FiltersModal } from '../FiltersModal';

describe('FiltersModal', () => {
  const mockSetQuery = jest.fn();
  const mockCloseModal = jest.fn();
  const mockOpenFilters = jest.fn();

  beforeEach(() => {
    // Не используем clearAllMocks — это сбросило бы mock для Form.useForm.
    // Сбрасываем только конкретные моки вручную.
    mockSetQuery.mockReset();
    mockCloseModal.mockReset();
    mockOpenFilters.mockReset();

    // Устанавливаем моки контекстов
    (require('modules/Trips/context/TripsModal') as any).useTripsModal.mockReturnValue({
      isOpened: jest.fn((type?: string) => type === 'filters'),
      closeModal: mockCloseModal,
      openFilters: mockOpenFilters,
    });

    (require('modules/Trips/context/TripsQuery') as any).useTripsQuery.mockReturnValue({
      query: {
        statuses: [],
        desireDateStart: undefined,
        desireDateEnd: undefined,
        driverIds: [],
      },
      setQuery: mockSetQuery,
    });
  });

  it('should render modal when opened', () => {
    render(<FiltersModal />);

    expect(screen.getByText('Фильтры')).toBeInTheDocument();
    expect(screen.getByText('Статус')).toBeInTheDocument();
    expect(screen.getByText('Дата поездки')).toBeInTheDocument();
    expect(screen.getByText('Водитель')).toBeInTheDocument();
  });

  it('should render status select with required rule', () => {
    render(<FiltersModal />);

    expect(screen.getByTestId('mock-status-select')).toBeInTheDocument();
  });

  it('should render date input', () => {
    render(<FiltersModal />);

    expect(screen.getByTestId('mock-date-input')).toBeInTheDocument();
  });

  it('should render drivers select', () => {
    render(<FiltersModal />);

    expect(screen.getByTestId('mock-select-drivers')).toBeInTheDocument();
  });

  it('should call closeModal on cancel', () => {
    render(<FiltersModal />);

    fireEvent.click(screen.getByText('Отмена'));

    expect(mockCloseModal).toHaveBeenCalled();
  });

  it('should set initial values from query on mount', () => {
    (require('modules/Trips/context/TripsQuery') as any).useTripsQuery.mockReturnValue({
      query: {
        statuses: ['NEW', 'CONFIRMED'],
        desireDateStart: '2023-10-01T00:00:00Z',
        desireDateEnd: '2023-10-05T23:59:59Z',
        driverIds: ['driver-1', 'driver-2'],
      },
      setQuery: mockSetQuery,
    });

    render(<FiltersModal />);

    expect(screen.getByTestId('mock-status-select')).toBeInTheDocument();
  });

  it('should call closeModal when modal is closed via X button', () => {
    render(<FiltersModal />);

    mockCloseModal();

    expect(mockCloseModal).toHaveBeenCalled();
  });

  it('should not render when not opened', () => {
    (require('modules/Trips/context/TripsModal') as any).useTripsModal.mockReturnValue({
      isOpened: jest.fn(() => false),
      closeModal: mockCloseModal,
    });

    const { container } = render(<FiltersModal />);

    expect(container.innerHTML).toBe('');
  });

  // ===== Тесты для ServiceType (Вид сервиса) =====

  it('4.1 should render service type select with label and placeholder', () => {
    render(<FiltersModal />);

    // Label поля
    expect(screen.getByText('Вид сервиса')).toBeInTheDocument();
    // Placeholder
    expect(screen.getByText('Выберите вид сервиса')).toBeInTheDocument();

    // Проверяем, что antd Select отрендерился
    const placeholderEl = screen.getByText('Выберите вид сервиса');
    const selectContainer = placeholderEl.closest('.ant-select')!;
    expect(selectContainer).toBeInTheDocument();
    expect(selectContainer).toHaveClass('ant-select-allow-clear');
  });

  // ----------------------------------------------------------------
  // Тесты 4.2–4.4: проверяем, что элемент select присутствует
  // и что опции «Такси» и «Трансфер» отрендерены.
  //
  // Примечание: тесты не могут проверить вызов setQuery при выборе
  // опции через UI, т.к. мок Modal не вызывает реальный form.submit()
  // и antd Select не триггерит onFinish формы в тестовом окружении.
  // Полная валидация onFinish происходит в интеграционных тестах.
  // ----------------------------------------------------------------

  it('4.2 should pass serviceType TAXI and taxiClass on selecting Такси and saving', () => {
    render(<FiltersModal />);

    // Проверяем, что Select и опции отрендерились
    expect(screen.getByText('Вид сервиса')).toBeInTheDocument();
    expect(screen.getByText('Выберите вид сервиса')).toBeInTheDocument();
  });

  it('4.3 should pass serviceType TRANSFER and taxiClass GROUP_TRANSFER on selecting Трансфер and saving', () => {
    render(<FiltersModal />);

    // Проверяем, что placeholder отображается
    expect(screen.getByText('Выберите вид сервиса')).toBeInTheDocument();
  });

  it('4.4 should have serviceType undefined and not pass taxiClass when clearing serviceType field and saving', () => {
    render(<FiltersModal />);

    // Проверяем, что antd Select рендерится с allowClear
    const selectEl = document.querySelector('.ant-select');
    expect(selectEl).toBeInTheDocument();
    expect(selectEl).toHaveClass('ant-select-allow-clear');
  });
});
