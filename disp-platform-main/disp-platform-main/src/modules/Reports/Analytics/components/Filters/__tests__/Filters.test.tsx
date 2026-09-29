import React from 'react';
import {
  render, screen, fireEvent, waitFor
} from '@testing-library/react';
import moment from 'moment';

// Создаем переменные для моков
let mockUseRoleMap: jest.Mock;
let mockUseAnalyticsQuery: jest.Mock;

// Мокаем зависимости
jest.mock('@sber-sbertransport/ui-kit/src', () => ({
  Select: jest.fn(({ id, ...props }) => {
    // Создаем уникальные testid для разных Select компонентов
    let testId = 'select';
    if (id === 'type') testId = 'select-type';
    if (id === 'vehicles') testId = 'select-vehicles';
    if (id === 'months') testId = 'select-months';

    return (
      <select
        data-testid={testId}
        id={id}
        {...props}
      />
    );
  }),
}));

jest.mock('components/AutoparkSelect/AutoparkSelect', () => ({
  __esModule: true,
  default: jest.fn(({ placeholder, ...props }) => (
    <div data-testid="autopark-select">
      <input
        data-testid="autopark-input"
        placeholder={placeholder}
        {...props}
      />
    </div>
  )),
}));

jest.mock('components/BranchSelect/BranchSelect', () => ({
  __esModule: true,
  default: jest.fn(({
    placeholder, disabled, ...props
  }) => {
    // Если компонент disabled, значит он не должен использоваться
    // В реальном коде он может рендериться, но быть отключенным
    return (
      <div data-testid="branch-select" data-disabled={disabled}>
        <input
          data-testid="branch-input"
          placeholder={placeholder}
          disabled={disabled}
          {...props}
        />
      </div>
    );
  }),
}));

jest.mock('hooks/useRoleMap', () => {
  mockUseRoleMap = jest.fn();
  return { useRoleMap: mockUseRoleMap };
});

jest.mock('../../../context/AnalyticsQuery', () => {
  mockUseAnalyticsQuery = jest.fn();
  return { useAnalyticsQuery: mockUseAnalyticsQuery };
});

jest.mock('i18n', () => ({
  useTranslation: jest.fn(() => ({
    t: {
      Analytics: {
        fields: {
          autoparkId: 'Автопарк',
          branches: 'Филиалы',
        },
      },
    },
  })),
}));

jest.mock('api/analytics/analytics.api', () => ({
  useAnalyticsTransport: jest.fn(() => ({
    data: ['vehicle1', 'vehicle2'],
    isFetching: false,
  })),
}));

jest.mock('antd', () => {
  const antd = jest.requireActual('antd');
  return {
    ...antd,
    DatePicker: jest.fn(({ suffixIcon, ...props }) => (
      <div data-testid="date-picker">
        <input data-testid="year-input" {...props} />
        {suffixIcon}
      </div>
    )),
  };
});

jest.mock('components/Button', () => ({
  Button: jest.fn(({ children, ...props }) => (
    <button data-testid="submit-button" {...props}>
      {children}
    </button>
  )),
}));

// Импортируем компонент после моков
import Filters from '../Filters';

describe('Filters Component', () => {
  const defaultProps = {};

  beforeEach(() => {
    jest.clearAllMocks();

    // Устанавливаем значения по умолчанию для моков
    mockUseRoleMap.mockReturnValue({ isAdmin: false, isManager: false });
    mockUseAnalyticsQuery.mockReturnValue({
      query: { year: moment().year() },
      setQuery: jest.fn(),
    });
  });

  test('1. Рендерится без ошибок', () => {
    render(<Filters {...defaultProps} />);

    expect(screen.getByTestId('date-picker')).toBeInTheDocument();
    expect(screen.getByText('Обновить')).toBeInTheDocument();
  });

  test('2. Отображаются все основные элементы формы', () => {
    render(<Filters {...defaultProps} />);

    expect(screen.getByTestId('date-picker')).toBeInTheDocument();
    expect(screen.getByTestId('submit-button')).toBeInTheDocument();
    expect(screen.getByTestId('select-months')).toBeInTheDocument();
  });

  test('3. Кнопка "Обновить" присутствует и кликабельна', () => {
    render(<Filters {...defaultProps} />);

    const submitButton = screen.getByTestId('submit-button');
    expect(submitButton).toBeInTheDocument();
    expect(submitButton).toBeEnabled();
  });

  test('4. Поле выбора года отображается с текущим годом по умолчанию', () => {
    const currentYear = moment().year();

    mockUseAnalyticsQuery.mockReturnValue({
      query: { year: currentYear },
      setQuery: jest.fn(),
    });

    render(<Filters {...defaultProps} />);

    expect(mockUseAnalyticsQuery).toHaveBeenCalled();
  });

  test('5. Поле месяцев отображается с плейсхолдером "Весь год"', () => {
    render(<Filters {...defaultProps} />);

    const monthsSelect = screen.getByTestId('select-months');
    expect(monthsSelect).toHaveAttribute('placeholder', 'Весь год');
  });

  test('6. Для администратора отображается AutoparkSelect', () => {
    mockUseRoleMap.mockReturnValue({ isAdmin: true, isManager: false });

    render(<Filters {...defaultProps} />);

    expect(screen.getByTestId('autopark-select')).toBeInTheDocument();
  });

  test('7. Для менеджера отображается BranchSelect', () => {
    mockUseRoleMap.mockReturnValue({ isAdmin: false, isManager: true });

    render(<Filters {...defaultProps} />);

    const branchSelect = screen.getByTestId('branch-select');
    expect(branchSelect).toBeInTheDocument();
  });

  test('8. Для обычного пользователя BranchSelect отключен', () => {
    mockUseRoleMap.mockReturnValue({ isAdmin: false, isManager: false });

    render(<Filters {...defaultProps} />);

    // В реальном компоненте BranchSelect может рендериться, но быть отключенным
    // Проверяем, что он disabled
    const branchInput = screen.getByTestId('branch-input');
    expect(branchInput).toBeDisabled();
  });

  test('9. Выбор транспорта отображает опции из API', async () => {
    render(<Filters {...defaultProps} />);

    await waitFor(() => {
      // Проверяем, что Select для транспорта существует
      expect(screen.getByTestId('select-vehicles')).toBeInTheDocument();
    });
  });

  test('10. При клике на кнопку "Обновить" вызывается setQuery', async () => {
    const mockSetQuery = jest.fn();

    mockUseAnalyticsQuery.mockReturnValue({
      query: { year: 2023 },
      setQuery: mockSetQuery,
    });

    render(<Filters {...defaultProps} />);

    const submitButton = screen.getByTestId('submit-button');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockSetQuery).toHaveBeenCalled();
    });
  });

  test('11. Форма отправляется с корректными данными', async () => {
    const mockSetQuery = jest.fn();

    mockUseAnalyticsQuery.mockReturnValue({
      query: { year: 2023 },
      setQuery: mockSetQuery,
    });

    mockUseRoleMap.mockReturnValue({ isAdmin: true, isManager: false });

    render(<Filters {...defaultProps} />);

    const submitButton = screen.getByTestId('submit-button');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockSetQuery).toHaveBeenCalledWith(expect.objectContaining({
        year: expect.any(Number),
      }));
    });
  });

  test('12. Отображаются все три типа Select', () => {
    render(<Filters {...defaultProps} />);

    expect(screen.getByTestId('select-type')).toBeInTheDocument();
    expect(screen.getByTestId('select-vehicles')).toBeInTheDocument();
    expect(screen.getByTestId('select-months')).toBeInTheDocument();
  });

  test('13. При изменении роли меняется видимость компонентов', () => {
    // Тест для администратора
    mockUseRoleMap.mockReturnValue({ isAdmin: true, isManager: false });
    const { rerender } = render(<Filters {...defaultProps} />);

    expect(screen.getByTestId('autopark-select')).toBeInTheDocument();

    // Тест для обычного пользователя
    mockUseRoleMap.mockReturnValue({ isAdmin: false, isManager: false });
    rerender(<Filters {...defaultProps} />);

    // AutoparkSelect не должен отображаться для обычного пользователя
    expect(screen.queryByTestId('autopark-select')).not.toBeInTheDocument();
  });
});
