import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import { TableStyled } from 'components/TableStyled';
import cn from 'classnames';
import { useColumns } from './useColumns';
import styles from './index.module.scss';
import { isVehicleBooked, isRequestDeadline } from './utils';
import withSuspense from 'components/withSuspense';
import withErrorBoundary from 'components/withErrorBoundary';
import { useActiveTrip } from '../../context/ActiveTrip';
import { useTripsQuery } from '../../context/TripsQuery';
import { TableProps } from 'antd/lib/table';
import { ColumnType, SorterResult } from 'antd/lib/table/interface';
import { useProfile } from 'api/profile/profile.api';
import { usePassTrips } from 'api/trips/trips.api';
import { PassTrip } from 'api/trips/trips.types';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { SortOrderToDirectionMap } from 'constants/app.constants';
import { useTripsSettings } from 'modules/Trips/context/TripsSettings.context';
import { useTripsContext } from 'context/Trips.context';
import { convertCamelToSnakeCase } from 'utils/convertStringCase';

interface RequestsTableProps {
  isSelfTripsVisible: boolean;
  getOptimizeColumns: (columns: ColumnType<PassTrip>[]) => ColumnType<PassTrip>[];
}

export const TripsTable: FC<RequestsTableProps> = withErrorBoundary(
  withSuspense(({ isSelfTripsVisible, getOptimizeColumns }) => {
    const { activeTrip, setActiveTrip } = useActiveTrip();
    const {
      query, setPagination, setSort,
    } = useTripsQuery();

    const { isVisible: isMapVisible } = useTripsSettings().settings.map;

    const { isOpened } = useTripsContext();

    const { id, contractorId } = useProfile().data;

    const {
      data: passTrips, refetch, isLoading, isFetching, isPreviousData,
    } = usePassTrips(
      {
        contractorId,
        dispatcherId: isSelfTripsVisible ? id : undefined,
        query,
      },
      {
        suspense: false,
        keepPreviousData: true,
      }
    );

    const [refetchCounter, setRefetchCounter] = useState(0);

    // Вместо refetchInterval в options, т.к. он криво работает при переходе между страницами
    // upd: setInterval изменен на setTimeout, т.к. интервал не проходит проверку SAST
    useEffect(() => {
      const update = () => {
        refetch();
        setRefetchCounter(prev => prev + 1);
      };

      let refetchTimer: NodeJS.Timeout;
      if (!isOpened) {
        refetchTimer = setTimeout(update, 60 * 1000);
      }

      return () => {
        clearInterval(refetchTimer);
      };
    }, [isOpened, refetchCounter, refetch]);

    const { passColumns } = useColumns(getOptimizeColumns);

    const getRowClassName = useCallback(
      (data: PassTrip) => {
        const isBooked = isVehicleBooked(data);
        const isDeadline = isRequestDeadline(data);

        return cn({
          [styles.active]: data.id === activeTrip?.id,
          [styles.deadline]: isDeadline,
          [styles.booked]: isBooked,
          [styles.new]: data.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT && !isDeadline,
        });
      },
      [activeTrip]
    );

    const onTableChange: TableProps<PassTrip>['onChange'] = (pagination, filters, sorter) => {
      const {
        columnKey, field, order,
      } = sorter as SorterResult<PassTrip>;

      setSort({
        field: convertCamelToSnakeCase(columnKey as string ?? field!),
        direction: order ? SortOrderToDirectionMap[order] : undefined,
      });
    };

    const onRow = useCallback(
      (trip: PassTrip) => ({
        onClick: () => {
          if (isMapVisible) {
            setActiveTrip(prev => (prev?.id === trip.id ? undefined : trip));
          }
        },
      }),
      [isMapVisible, setActiveTrip]
    );

    return (
      <div className={cn(styles.tableWrapper, { [styles.fullWidth]: !isMapVisible })}>
        <TableStyled
          className={styles.requestTable}
          paginationParams={query}
          total={passTrips?.totalElements}
          setPagination={setPagination}
          dataSource={passTrips?.content}
          columns={passColumns}
          rowKey="id"
          rowClassName={getRowClassName}
          onRow={onRow}
          loading={isLoading || (isFetching && isPreviousData)}
          onChange={onTableChange}
          autoHeight
        />
      </div>
    );
  })
);
