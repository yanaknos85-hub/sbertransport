import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import { useBrandsStatistics, useVehicleAnalytics } from 'api/analytics/analytics.api';
import ChartCard from '../../components/ChartCard/ChartCard';
import BarChart from '../../components/BarChart';
import { formatVehicleToChartData } from '../../utils/formatToChartData';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';

const VehicleAnalytics: FC = () => {
  const t = useTranslation().t.Analytics;

  const { query } = useAnalyticsQuery();

  const { data } = useVehicleAnalytics(query);

  const brandStatistics = useBrandsStatistics(query, { suspense: false }).data;

  const chartData = formatVehicleToChartData(data, true);

  return (
    <ChartCard
      title={t.Vehicle.title}
      tooltipProps={{ title: t.Vehicle.tooltipText }}
    >
      <BarChart
        data={chartData}
        period="month"
        showBarValue
        tooltipProps={{
          rangeSign: 'шт',
          year: query.year,
          list: activeIndex => [
            ...(brandStatistics
              ?.find(monthStatistics => monthStatistics.month === activeIndex + 1)
              ?.brands
              .map(brand => ({ label: brand.brand, value: brand.count })) ?? []),
            {
              label: 'ИТОГО',
              value: brandStatistics
                ?.find(monthStatistics => monthStatistics.month === activeIndex + 1)
                ?.totalCount ?? 0,
              fontWeight: 'bold',
            },
          ],
        }}
      />
    </ChartCard>
  );
};

export default VehicleAnalytics;
