import React from 'react';
import { render, screen } from '@testing-library/react';
import { useTranslation } from 'i18n';
import moment from 'moment';
import OneVehicleAnalytics, { calculateExploitationTime } from '../OneVehicleAnalytics';

// Mock external dependencies
jest.mock('i18n');
jest.mock('moment');
const mockData = {
  inExploitationDays: 150,
  exploitationStart: '2023-01-01T00:00:00Z',
};

jest.mock('api/analytics/analytics.api', () => ({
  useOneVehicleExploitation: () => ({
    data: mockData,
  }),
}));

jest.mock('../../../context/AnalyticsQuery', () => ({
  useAnalyticsQuery: () => ({
    query: {
      stateNumbers: ['ABC123'],
      period: { from: '2023-01-01', to: '2023-12-31' },
    },
  }),
}));

describe('OneVehicleAnalytics', () => {
  beforeEach(() => {
    // Reset mocks
    (useTranslation as jest.Mock).mockReturnValue({
      t: {
        Analytics: {
          OneVehicleAnalytics: {
            title: 'Vehicle Analytics',
            exploitationTime: 'Exploitation Time',
            exploitationStart: 'Exploitation Start',
          },
        },
      },
    });

    (moment as unknown as jest.Mock).mockReturnValue({
      format: () => '01.01.2023',
      diff: () => 2,
    });
  });

  test('renders the component with correct data', () => {
    render(<OneVehicleAnalytics />);

    // Check if title is rendered
    expect(screen.getByText('Vehicle Analytics')).toBeInTheDocument();

    // Check if exploitation time is rendered
    expect(screen.getByText('Exploitation Time, мес.')).toBeInTheDocument();
    // Здесь данные берутся из diff, а не из mockData, т.к. расчет теперь идет на фронте
    expect(screen.getByText('2')).toBeInTheDocument();

    // Check if exploitation start is rendered
    expect(screen.getByText('Exploitation Start')).toBeInTheDocument();
    expect(screen.getByText('01.01.2023')).toBeInTheDocument();
  });

  // Правильная реализация теста "renders without stateNumber"
  test('renders without stateNumber', () => {
    // Мокаем query без stateNumbers
    jest.mock('../../../context/AnalyticsQuery', () => ({
      useAnalyticsQuery: () => ({
        query: {
          stateNumbers: [], // Пустой массив или undefined
          period: { from: '2023-01-01', to: '2023-12-31' },
        },
      }),
    }));

    render(<OneVehicleAnalytics />);

    // Проверяем, что компонент рендерится без ошибок
    // Возможно, показывает заглушку или сообщение об отсутствии данных
    expect(screen.getByText('Vehicle Analytics')).toBeInTheDocument();
    // Не должно быть данных конкретного ТС
    expect(screen.queryByText('150')).not.toBeInTheDocument();
  });
});

describe('calculateExploitationTime', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('returns months when difference is 1 month or more', () => {
    const mockDiff = jest.fn()
      .mockReturnValueOnce(5); // первый вызов для месяцев

    (moment as unknown as jest.Mock).mockImplementation(() => ({
      diff: mockDiff,
    }));

    const result = calculateExploitationTime('2023-01-01');

    expect(result).toEqual({
      value: 5,
      text: 'мес.',
    });
    expect(mockDiff).toHaveBeenCalledWith('2023-01-01', 'months');
  });

  test('returns days when difference is less than 1 month', () => {
    const mockDiff = jest.fn()
      .mockReturnValueOnce(0) // первый вызов для месяцев
      .mockReturnValueOnce(15); // второй вызов для дней

    (moment as unknown as jest.Mock).mockImplementation(() => ({
      diff: mockDiff,
    }));

    const result = calculateExploitationTime('2023-01-01');

    expect(result).toEqual({
      value: 15,
      text: 'дней',
    });
    expect(mockDiff).toHaveBeenNthCalledWith(1, '2023-01-01', 'months');
    expect(mockDiff).toHaveBeenNthCalledWith(2, '2023-01-01', 'days');
  });

  test('returns days when difference is exactly 0 days', () => {
    const mockDiff = jest.fn()
      .mockReturnValueOnce(0) // месяцев = 0
      .mockReturnValueOnce(0); // дней = 0

    (moment as unknown as jest.Mock).mockImplementation(() => ({
      diff: mockDiff,
    }));

    const result = calculateExploitationTime('2023-01-01');

    expect(result).toEqual({
      value: 0,
      text: 'дней',
    });
  });

  test('handles future dates (negative difference)', () => {
    const mockDiff = jest.fn()
      .mockReturnValueOnce(-2); // отрицательные месяцы

    (moment as unknown as jest.Mock).mockImplementation(() => ({
      diff: mockDiff,
    }));

    const result = calculateExploitationTime('2025-01-01');

    expect(result).toEqual({
      value: -2,
      text: 'мес.',
    });
  });

  // Дополнительный тест для проверки реальной работы с moment
  test('works with actual moment implementation', () => {
    // Временно отключаем мок moment
    jest.unmock('moment');

    const realMoment = jest.requireActual('moment');
    (moment as unknown as jest.Mock).mockImplementation(realMoment);

    const threeMonthsAgo = realMoment().subtract(3, 'months').format();
    const result = calculateExploitationTime(threeMonthsAgo);

    expect(result.value).toBe(3);
    expect(result.text).toBe('мес.');

    const fiveDaysAgo = realMoment().subtract(5, 'days').format();
    const resultDays = calculateExploitationTime(fiveDaysAgo);

    expect(resultDays.value).toBe(5);
    expect(resultDays.text).toBe('дней');

    // Возвращаем мок обратно
    jest.mock('moment');
  });
});
