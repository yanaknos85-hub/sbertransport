import React, { FC, useState } from 'react';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import { useFuelConsumptionAnalytics } from 'api/analytics/analytics.api';
import ChartCard from '../../components/ChartCard/ChartCard';
import BarChart from '../../components/BarChart';
import { formatFuelConsumptionToChartData } from '../../utils/formatToChartData';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';

import styles from './FuelConsumptionAnalytics.module.scss';

import mockData from './mockData.json'; // TODO удалить когда данные будут приходить с бэка

export enum IndicatorTypes {
  LITERS = 'LITERS',
  RUBLES = 'RUBLES',
}

const indicators = Object.keys(IndicatorTypes);

const FuelConsumptionAnalytics: FC = () => {
  const t = useTranslation().t.Analytics;

  const [activeIndicator, setActiveIndicator] = useState(IndicatorTypes.LITERS);

  const { query } = useAnalyticsQuery();

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { data } = useFuelConsumptionAnalytics(query, {
    enabled: false, // Пока бэк не готов используем mockData
  });

  const chartData = formatFuelConsumptionToChartData(mockData, activeIndicator);

  const onFilters = (type: IndicatorTypes) => () => {
    setActiveIndicator(type);
  };

  return (
    <ChartCard
      title={t.FuelConsumption.title}
      tooltipProps={{ title: t.FuelConsumption.tooltipText }}
      actions={(
        <div className={styles.actions}>
          {indicators.map(type => (
            <span
              key={type}
              className={cn(styles.indicator, {
                [styles.indicatorActive]: type === activeIndicator,
              })}
              onClick={onFilters(type as IndicatorTypes)}
            >
              {t.FuelConsumption.Filters[type.toLocaleLowerCase()]}
            </span>
          ))}
        </div>
      )}
    >
      <BarChart
        data={chartData}
        period="month"
        rangeSign={activeIndicator === IndicatorTypes.LITERS ? 'л' : '₽'}
        showBarValue
      />
    </ChartCard>
  );
};

export default FuelConsumptionAnalytics;
