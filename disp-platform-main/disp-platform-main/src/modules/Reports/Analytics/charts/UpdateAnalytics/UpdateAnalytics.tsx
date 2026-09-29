import React, { FC, useState } from 'react';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { useTranslation } from 'i18n';

import { useVehicleAnalytics } from 'api/analytics/analytics.api';
import Checkbox from 'components/Checkbox/Checkbox';
import ChartCard from '../../components/ChartCard/ChartCard';
import BarChart from '../../components/BarChart';
import { formatVehicleToChartData } from '../../utils/formatToChartData';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';

import styles from './UpdateAnalytics.module.scss';

const UpdateAnalytics: FC = () => {
  const t = useTranslation().t.Analytics;

  const { query } = useAnalyticsQuery();

  const [filters, setFilters] = useState({ showActive: true, showInActive: true });

  const { data } = useVehicleAnalytics(query);

  const chartData = formatVehicleToChartData(data);

  const onFilters = (type: string) => (e: CheckboxChangeEvent) => {
    setFilters({ ...filters, [type]: e.target.checked });
  };

  return (
    <ChartCard
      title={t.Update.title}
      tooltipProps={{ title: t.Update.tooltipText }}
      actions={(
        <div className={styles.actions}>
          <Checkbox
            checked={filters.showActive}
            className={styles.activeCheckbox}
            onChange={onFilters('showActive')}
          >
            {t.Update.Filters.active}
          </Checkbox>
          <Checkbox
            checked={filters.showInActive}
            className={styles.inActiveCheckbox}
            onChange={onFilters('showInActive')}
          >
            {t.Update.Filters.inActive}
          </Checkbox>
        </div>
      )}
    >
      <BarChart
        data={chartData}
        period="month"
        multiple
        separate
        barWidth={58}
        spacing={2}
        currentBarWidth={58}
        minRangeWidth={44}
        filters={{
          showPrimary: filters.showActive,
          showSecondary: filters.showInActive,
        }}
        tooltipProps={{
          rangeSign: 'шт',
          primaryText: t.Update.Filters.active,
          secondaryText: t.Update.Filters.inActive,
          year: query.year,
        }}
      />
    </ChartCard>
  );
};

export default UpdateAnalytics;
