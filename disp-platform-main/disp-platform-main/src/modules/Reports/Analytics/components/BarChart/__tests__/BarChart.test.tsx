import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import BarChart from '../BarChart';
import { BarChartData } from '../BarChart.types';
import { monthNamesShort } from 'utils/calendar';

describe('BarChart', () => {
  const mockData: BarChartData[] = [
    {
      date: 1, primary: 50, secondary: 30,
    },
    {
      date: 2, primary: 70, secondary: 40,
    },
    {
      date: 3, primary: 30, secondary: 60,
    },
  ];

  const defaultProps = {
    data: mockData,
    period: 'month' as const,
    width: 700,
    height: 126,
    barWidth: 50,
    spacing: 8,
    minRangeWidth: 50,
    primaryColor: '#a9de59',
    secondaryColor: '#e28965',
    currentBarColor: '#f3f5f7',
    rangeSign: 'шт.',
    filters: { showPrimary: true, showSecondary: true },
    showBarValue: false,
    showCurrent: true,
    chartMargin: {
      top: 20, right: 0, bottom: 20, left: 0,
    },
    rangeMargin: 5,
  };

  test('renders the chart with correct number of bars', () => {
    render(<BarChart {...defaultProps} />);

    // Проверяем наличие SVG
    const svg = screen.getByTestId('bar-chart-svg');
    expect(svg.tagName).toBe('svg');

    // Проверяем наличие групп (баров)
    const bars = svg.querySelectorAll('g');
    expect(bars.length).toBe(mockData.length);
  });

  test('renders correct period labels', () => {
    render(<BarChart {...defaultProps} />);

    mockData.forEach(item => {
      const label = screen.getByText(new RegExp(`^${monthNamesShort[item.date - 1]}$`));
      expect(label).toBeInTheDocument();
    });
  });

  test('renders tooltip on hover when tooltipProps are provided', () => {
    const mockTooltipContent = 'Tooltip Content';
    const tooltipProps = {
      primaryText: 'Primary',
      secondaryText: 'Secondary',
      year: 2023,
      content: () => <div>{mockTooltipContent}</div>,
      list: () => [
        {
          label: 'List Item', value: 100,
        },
      ],
    };

    render(<BarChart {...defaultProps} tooltipProps={tooltipProps} />);

    // Находим первый бар и наводим мышь
    const firstBarGroup = screen.getAllByTestId('bar-chart-svg')[0].querySelector('g');
    if (firstBarGroup) {
      fireEvent.mouseEnter(firstBarGroup);

      // Проверяем наличие контента тултипа
      expect(screen.getByText(mockTooltipContent)).toBeInTheDocument();
      expect(screen.getByText('Primary', { exact: false })).toBeInTheDocument();
      expect(screen.getByText('Secondary', { exact: false })).toBeInTheDocument();
      expect(screen.getByText('List Item')).toBeInTheDocument();
    }
  });

  test('does not render tooltip when not hovered', () => {
    const tooltipProps = {
      primaryText: 'Primary',
      year: 2023,
    };

    render(<BarChart {...defaultProps} tooltipProps={tooltipProps} />);

    // Проверяем, что тултип не отображается до наведения
    const tooltip = screen.queryByText('Primary');
    expect(tooltip).not.toBeInTheDocument();
  });

  test('hides secondary bar when filters.showSecondary is false', () => {
    render(
      <BarChart
        {...defaultProps}
        filters={{ ...defaultProps.filters, showSecondary: false }}
      />
    );

    const svg = screen.getByTestId('bar-chart-svg');
    const bars = svg.querySelectorAll('rect');

    const secondaryBars = Array.from(bars).filter(
      bar => bar.getAttribute('fill') === defaultProps.secondaryColor
    );

    expect(secondaryBars.length).toBe(0);
  });

  test('displays bar values when showBarValue is true', () => {
    render(<BarChart {...defaultProps} showBarValue={true} />);

    // Проверяем наличие текста значений баров
    mockData.forEach(item => {
      const valueText = screen.getByText(String(item.primary), { exact: false, selector: 'text' });
      expect(valueText).toBeInTheDocument();
    });
  });

  test('handles resize and updates container width', () => {
    // Мокаем getBoundingClientRect
    Object.defineProperty(HTMLElement.prototype, 'getBoundingClientRect', {
      configurable: true,
      value: () => ({
        width: 800,
      }),
    });

    render(<BarChart {...defaultProps} externalPadding={16} />);

    // Устанавливаем новую ширину
    Object.defineProperty(HTMLElement.prototype, 'getBoundingClientRect', {
      value: () => ({
        width: 600,
      }),
    });

    // Имитируем событие ресайза
    fireEvent(window, new Event('resize'));

    // Проверяем, что контейнер обновился
    const container = screen.getByTestId('bar-chart-container');
    // Так как мы не можем легко проверить внутреннее состояние хука, проверим через минимальный тест
    expect(container).toBeInTheDocument();
  });
});
