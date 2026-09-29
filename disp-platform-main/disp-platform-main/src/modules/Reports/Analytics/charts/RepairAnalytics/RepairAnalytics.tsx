import React, { FC, useState } from 'react';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { useTranslation } from 'i18n';

import { useRepairAnalytics } from 'api/analytics/analytics.api';
import Checkbox from 'components/Checkbox/Checkbox';
import SelectMonth from 'modules/Reports/Analytics/components/SelectMonth/SelectMonth';
import ChartCard from '../../components/ChartCard/ChartCard';
import BarChart from '../../components/BarChart';
import { formatRepairToChartData } from '../../utils/formatToChartData';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';

import styles from './RepairAnalytics.module.scss';

const RepairAnalytics: FC = () => {
  const t = useTranslation().t.Analytics;

  const [filters, setFilters] = useState({ showRepair: true, showExploitation: true });
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth());

  const { query } = useAnalyticsQuery();

  const { data } = useRepairAnalytics(query);

  const chartData = formatRepairToChartData(data, selectedMonth, query.year);

  const onFilters = (type: string) => (e: CheckboxChangeEvent) => {
    setFilters({ ...filters, [type]: e.target.checked });
  };

  return (
    <ChartCard
      title={t.Repair.title}
      tooltipProps={{ title: t.Repair.tooltipText }}
      actions={(
        <div className={styles.actions}>
          <Checkbox
            checked={filters.showExploitation}
            className={styles.availableCheckbox}
            onChange={onFilters('showExploitation')}
          >
            {t.Repair.Filters.exploitation}
          </Checkbox>
          <Checkbox
            checked={filters.showRepair}
            className={styles.repairCheckbox}
            onChange={onFilters('showRepair')}
          >
            {t.Repair.Filters.repair}
          </Checkbox>
        </div>
      )}
    >
      <SelectMonth
        value={selectedMonth}
        onChange={setSelectedMonth}
        className={styles.selectMonth}
      />
      <BarChart
        data={chartData}
        period="day"
        multiple
        barWidth={18}
        spacing={4}
        currentBarWidth={20}
        filters={{
          showPrimary: filters.showExploitation,
          showSecondary: filters.showRepair,
        }}
        tooltipProps={{
          rangeSign: 'шт',
          primaryText: t.Repair.Filters.exploitation,
          secondaryText: t.Repair.Filters.repair,
          month: selectedMonth,
          year: query.year,
        }}
      />
    </ChartCard>
  );
};

export default RepairAnalytics;
