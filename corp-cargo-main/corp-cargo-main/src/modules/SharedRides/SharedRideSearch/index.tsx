import { Table } from 'antd';
import * as R from 'ramda';
import React, {
  useState, Suspense, useRef, useEffect
} from 'react';
import { useRouteMatch, Link } from 'react-router-dom';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useSearchSharedRides, SharedRideSearchParams } from 'api/shared-rides';
import { SharedRide } from 'stores/SharedRide/SharedRide.interface';
import { useTranslation } from 'i18n';
import { EyeOutlined } from '@ant-design/icons';
import { FilterPanel } from 'shared/components/FilterPanel';

import { useColumns } from './useColumns';
import { useFilterFields } from './Components/useFilterFields';

import styles from './Components/styles.module.scss';

type FilterValues = Partial<Record<keyof SharedRide, any>>;

const convertFiltersForm = (filters: any): FilterValues => {
  if (R.isEmpty(filters)) {
    return {};
  }
  const addressStart = {
    address: filters?.addressStart,
    latitude: filters['fullData-addressStart']?.latitude,
    longitude: filters['fullData-addressStart']?.longitude,
    waitingTimeMin: 0,
  };
  const addressEnd = {
    address: filters?.addressEnd,
    latitude: filters['fullData-addressEnd']?.latitude,
    longitude: filters['fullData-addressEnd']?.longitude,
    waitingTimeMin: 0,
  };
  const addresses
    = filters.keys?.map((key: string) => ({
      address: filters[`address--${key}`],
      latitude: filters[`fullData-address--${key}`]?.latitude,
      longitude: filters[`fullData-address--${key}`]?.longitude,
      waitingTimeMin: filters[`address--${key}-waitTime`],
    })) || [];
  return {
    passengers: filters.passengers,
    tariffId: filters.tariffId,
    // TODO add correct format time after backend endpoint ready
    pickupStartTime: filters.pickupStartTime,
    dropStartTime: filters.dropStartTime,
    stops: [addressStart, ...addresses, addressEnd],
  };
};

const SharedRideSearchInner: React.FC<{ filters: SharedRideSearchParams }> = ({ filters }) => {
  const match = useRouteMatch();

  // TODO fix after ready end-point on backend
  const {
    data: { sharedRides },
    refetch,
  } = useSearchSharedRides(filters);

  const extendButtons = {
    fixed: 'right',
    width: 60,
    render: (_: any, record: any): JSX.Element => (
      <Link to={`${match.path}/${record.id}`}>
        <EyeOutlined />
      </Link>
    ),
  };

  const columns = useColumns();
  const columnsProps = columnsPropsFactory<SharedRide>([...columns, extendButtons as any]);

  // TODO test refetch on change filters
  useEffect(() => {
    refetch();
  }, [filters]);

  return (
    <Table
      columns={columnsProps}
      dataSource={sharedRides}
      bordered
      style={{ overflow: 'scroll' }}
      scroll={{ x: 600 }}
      rowKey="id"
      size="small"
      tableLayout="auto"
      className={styles.table}
      pagination={false}
    />
  );
};

export const SharedRideSearch = (): JSX.Element => {
  const errorBoundaryRef = useRef<ErrorBoundary>(null);
  const { t } = useTranslation();

  const [filters, setFilters] = useState<any>({});
  const correctFilters = convertFiltersForm(filters);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <FilterPanel
        fields={useFilterFields()}
        className={styles.filterPanel}
        onApplyFilters={values => {
          errorBoundaryRef.current?.reset();
          setFilters(values);
        }}
      />
      <ErrorBoundary ref={errorBoundaryRef} fallback={() => <span>{t.SharedRidesSearch.ErrorText}</span>}>
        <SharedRideSearchInner filters={correctFilters} />
      </ErrorBoundary>
    </Suspense>
  );
};
