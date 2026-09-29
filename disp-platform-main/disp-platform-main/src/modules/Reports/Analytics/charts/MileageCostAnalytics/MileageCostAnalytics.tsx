import React, { FC, useState } from 'react';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { useTranslation } from 'i18n';

import { useMileageCostAnalytics } from 'api/analytics/analytics.api';
import Checkbox from 'components/Checkbox/Checkbox';
import ChartCard from '../../components/ChartCard/ChartCard';
import BarChart from '../../components/BarChart';
import { formatMileageCostToChartData } from '../../utils/formatToChartData';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';

import styles from './MileageCostAnalytics.module.scss';

import mockData from './mockData.json'; // TODO удалить когда данные будут приходить с бэка

const MileageCostAnalytics: FC = () => {
  const t = useTranslation().t.Analytics;

  const [filters, setFilters] = useState({ showFuel: true, showContent: true });

  const { query } = useAnalyticsQuery();

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { data } = useMileageCostAnalytics(query, {
    enabled: false, // Пока бэк не готов используем mockData
  });

  const chartData = formatMileageCostToChartData(mockData);

  const onFilters = (type: string) => (e: CheckboxChangeEvent) => {
    setFilters({ ...filters, [type]: e.target.checked });
  };

  return (
    <ChartCard
      title={t.MileageCost.title}
      tooltipProps={{ title: t.MileageCost.tooltipText }}
      actions={(
        <div className={styles.actions}>
          <Checkbox
            checked={filters.showFuel}
            className={styles.fuelCheckbox}
            onChange={onFilters('showFuel')}
          >
            {t.MileageCost.Filters.fuel}
          </Checkbox>
          <Checkbox
            checked={filters.showContent}
            className={styles.contentCheckbox}
            onChange={onFilters('showContent')}
          >
            {t.MileageCost.Filters.content}
          </Checkbox>
        </div>
      )}
    >
      <BarChart
        data={chartData}
        multiple
        showBarValue
        period="month"
        rangeSign="₽"
        primaryColor="#FFC914"
        secondaryColor="#FFE489"
        filters={{
          showPrimary: filters.showFuel,
          showSecondary: filters.showContent,
        }}
        tooltipProps={{
          primaryText: t.MileageCost.Filters.fuel,
          secondaryText: t.MileageCost.Filters.content,
          year: query.year,
        }}
      />
    </ChartCard>
  );
};

export default MileageCostAnalytics;
