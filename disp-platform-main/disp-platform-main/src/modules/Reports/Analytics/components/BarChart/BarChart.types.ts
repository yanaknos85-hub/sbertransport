import { ReactNode } from 'react';

type ChartPeriodType = 'day' | 'month';

export interface BarChartData {
  /** Номер дня или месяца */
  date: number;
  /** Значение первого показателя */
  primary: number;
  /** Значение второго показателя */
  secondary: number;
  /** Текущий месяц */
  selectedMonth?: number;
}

export interface FilterSettings {
  /**  Флаг отображения основного показателя */
  showPrimary: boolean;
  /**  Флаг отображения вторичного показателя */
  showSecondary: boolean;
}

export interface TooltipProps {
  primaryText?: string;
  secondaryText?: string;
  month?: number;
  year?: number;
  /** Произвольный контент для отображения */
  content?: (activeIndex: number) => ReactNode;
  /** Кастомный список для отображения */
  list?: (activeIndex: number) => ({
    label: string;
    value: string | number;
    background?: string;
    fontWeight?: number | string;
  })[] | undefined;
  /** Единица измерения (шт, кг, км, ...) */
  rangeSign?: string;
}

export interface BarChartProps {
  data: BarChartData[];
  /**  Тип периода */
  period: ChartPeriodType;
  /**  Флаг наличия нескольких показателей */
  multiple?: boolean;
  /**  Флаг раздельности показателей */
  separate?: boolean;
  /**  Цвет основного показателя */
  primaryColor?: string;
  /**  Цвет вторичного показателя */
  secondaryColor?: string;
  /**  Цвет текущего периода */
  currentBarColor?: string;
  /**  Парметры фильтрации отображаемых показателей */
  filters?: FilterSettings;
  /**  Данные для отображения в подсказке */
  tooltipProps?: TooltipProps;
  /**  Флаг отображающий значение показателя */
  showBarValue?: boolean;
  /**  Флаг отвечающий за выделение текущего периода */
  showCurrent?: boolean;
  /**  Ограниченная ширина */
  width?: number;
  /** Ограниченная высота */
  height?: number;
  /**  Ширина одного столбца */
  barWidth?: number;
  /** Расстояние между столбцами */
  spacing?: number;
  /**  Ширина столбца текущего месяца */
  currentBarWidth?: number;
  /**  Минимальный размер шкалы с диапазонами */
  minRangeWidth?: number;
  /** Единица измерения (шт, кг, км, ...) */
  rangeSign?: string;
  /** Внутренний отступ показателя */
  separatePadding?: number;
  /** Отступы графика */
  chartMargin?: Record<string, number>;
  /** Внешний padding */
  externalPadding?: number;
  /** Внешний отступ шкалы от графика */
  rangeMargin?: number;
}
