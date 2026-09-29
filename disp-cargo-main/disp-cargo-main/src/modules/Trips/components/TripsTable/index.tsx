import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import { TableStyled } from 'components/TableStyled';
import cn from 'classnames';
import { useColumns } from './useColumns';
import styles from './index.module.scss';
import { isRequestDeadline } from './utils';
import { useActiveTrip } from '../../context/ActiveTrip';
import { useTripsQuery } from '../../context/TripsQuery';
import { TableProps } from 'antd/lib/table';
import { SorterResult } from 'antd/lib/table/interface';
import { columnToSortMap } from '../../constants';
import withErrorBoundary from 'components/withErrorBoundary';
import withSuspense from 'components/withSuspense';
import { useTripsContext } from 'context/Trips.context';
import { useProfile } from 'api/profile/profile.api';
import { useCargoTrips } from 'api/trips-cargo/trips-cargo.api';
import { CargoTrip } from 'api/trips-cargo/trips-cargo.types';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { SortOrderToDirectionMap } from 'constants/app.constants';
import { useTripsSettings } from 'modules/Trips/context/TripsSettings.context';

interface RequestsTableProps {
  isSelfTripsVisible: boolean;
}

export const TripsTable: FC<RequestsTableProps> = withErrorBoundary(
  withSuspense(({ isSelfTripsVisible }) => {
    const { activeTrip, setActiveTrip } = useActiveTrip();
    const {
      query, setPagination, setSort,
    } = useTripsQuery();

    const { isVisible: isMapVisible } = useTripsSettings().settings.map;

    const { isOpened } = useTripsContext();

    const { id, contractorId } = useProfile().data;

    const {
      data: cargoTrips, refetch, isLoading, isFetching, isPreviousData,
    } = useCargoTrips(
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

    const { cargoColumns } = useColumns();

    const getRowClassName = useCallback(
      (data: CargoTrip) => {
        const isDeadline = isRequestDeadline(data);

        return cn({
          [styles.active]: data.id === activeTrip?.id,
          [styles.deadline]: isDeadline,
          [styles.new]: data.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT && !isDeadline,
        });
      },
      [activeTrip]
    );

    const onTableChange: TableProps<CargoTrip>['onChange'] = (pagination, filters, sorter) => {
      const {
        columnKey, field, order,
      } = sorter as SorterResult<CargoTrip>;

      setSort({
        field: columnToSortMap[columnKey as string ?? field!],
        direction: order ? SortOrderToDirectionMap[order] : undefined,
      });
    };

    const onRow = useCallback(
      (trip: CargoTrip) => ({
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
          total={cargoTrips?.totalElements}
          setPagination={setPagination}
          dataSource={cargoTrips?.content}
          columns={cargoColumns}
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
