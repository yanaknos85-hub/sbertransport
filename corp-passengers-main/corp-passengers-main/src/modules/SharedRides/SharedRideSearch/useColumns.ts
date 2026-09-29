import { useMemo } from 'react';
import { ColumnProps } from 'antd/lib/table';

import { useTranslation } from 'i18n';
import { SharedRide, SharedRideStop } from 'stores/SharedRide/SharedRide.interface';
import { TaxiClassDescriptions, TaxiClass } from 'stores/Trip/Trip.interface';

export const useColumns = (): ColumnProps<SharedRide>[] => {
  const { t } = useTranslation();

  return useMemo(
    () => [
      {
        dataIndex: 'passengers',
        title: t.SharedRidesSearch.Columns.passengers,
        key: 'passengers',
        width: 140,
      },
      {
        dataIndex: 'stops',
        title: t.SharedRidesSearch.Columns.stops,
        key: 'stops',
        width: 220,
        render: (stops: SharedRideStop[]): string => stops && stops.length ? stops.map(stop => stop.address).join(' - ') : '-',
      },
      {
        dataIndex: 'tariffId',
        title: t.SharedRidesSearch.Columns.tariffId,
        key: 'tariffId',
        width: 140,
        render: (value: TaxiClass): string => TaxiClassDescriptions[value],
      },
      {
        dataIndex: 'pickupStartTime',
        title: t.SharedRidesSearch.Columns.pickupStartTime,
        key: 'pickupStartTime',
        width: 140,
      },
      {
        dataIndex: 'dropStartTime',
        title: t.SharedRidesSearch.Columns.dropStartTime,
        key: 'dropStartTime',
        width: 140,
      },
    ],
    [t]
  );
};
