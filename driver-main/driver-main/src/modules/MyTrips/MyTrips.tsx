import {
  FC, useMemo, useRef, useEffect
} from 'react';
import { useNavigate, useParams } from 'react-router';
import { observer } from 'mobx-react';
import { Skeleton, Tabs } from 'antd-mobile';
import {
  List as VirtualizedList, AutoSizer, InfiniteLoader, CellMeasurer, CellMeasurerCache
} from 'react-virtualized';
import dayjs from 'dayjs';

import { useBusyness, useInfiniteTripList } from 'api/services/Trips/Trips.query';
import { BusynessTrip, PassTrip } from 'api/services/Trips/Trips.types';
import { activeTripStatuses, finalTripStatuses, TripTabs } from 'constants/trips.constants';
import { routes } from 'constants/routes.constants';
import NavBar from 'components/NavBar';
import TripCard from 'components/TripCard';
import Empty from 'components/Empty';
import Spin from 'components/Spin';
import { useAppStore } from 'stores/stores.context';
import { ignore } from 'utils';

import styles from './MyTrips.module.scss';

// Интервал обновления поездок, пока нет вебсокетов
const REFETCH_INTERVAL = 30000;
const BUSYNESS_START_TIME = dayjs().toISOString();
const BUSYNESS_END_TIME = dayjs().add(1, 'w').toISOString();

interface PassTripsProps {
  mapCenter: [number, number];
}

const cache = new CellMeasurerCache({
  fixedWidth: true,
  defaultHeight: 350,
});

const PassTrips: FC<PassTripsProps> = ({ mapCenter }) => {
  const { type } = useParams<{ type: string }>();
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => () => {
    cache.clearAll();
  }, []);

  const {
    data, fetchNextPage, hasNextPage, isFetching, isFetchingNextPage, isInitialLoading,
  } = useInfiniteTripList({
    statuses: type === TripTabs.InActive ? finalTripStatuses : activeTripStatuses,
  });

  const businessTrips = useBusyness(
    {
      startTime: BUSYNESS_START_TIME,
      endTime: BUSYNESS_END_TIME,
    },
    {
      refetchInterval: REFETCH_INTERVAL,
      enabled: type === TripTabs.Active,
    }
  ).data;

  const {
    trips, totalElements, tripsLength,
  } = useMemo(() => {
    let trips: (PassTrip | BusynessTrip)[] = data?.pages.flatMap(page => page.content) || [];
    const busyness = businessTrips?.busyness?.[0]?.trips ?? null;

    const tripsLength = data?.pages[0]?.totalElements || 0;
    let totalElements = tripsLength;

    if (busyness) {
      trips = trips.concat(busyness);
      totalElements = totalElements + busyness.length;
    }

    return {
      trips,
      totalElements,
      tripsLength,
    };
  }, [data, businessTrips]);

  const isRowLoaded = ({ index }) => trips[index] !== undefined;

  const loadMoreRows = () => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage().then(cache.clearAll).catch(ignore);
    }
  };

  const rowRenderer = ({
    index, key, style, parent,
  }) => (
    <CellMeasurer
      key={key}
      cache={cache}
      parent={parent}
      columnIndex={0}
      rowIndex={index}
    >
      {({ registerChild }) => (
        <div style={{ ...style, paddingBottom: 20 }} ref={registerChild}>
          {trips[index] ? (
            <TripCard
              trip={trips[index]}
              mapCenter={mapCenter}
              isBusynessTrip={type === TripTabs.Active ? Boolean(index >= tripsLength && businessTrips) : false}
            />
          ) : (
            <Skeleton.Paragraph lineCount={12} animated />
          )}
        </div>
      )}
    </CellMeasurer>
  );

  if (isInitialLoading) return <Spin />;

  if (!trips.length && !isFetching) {
    return (
      <Empty
        title="У вас еще нет заказов."
        description="Ожидайте назначения на Вас заказов диспетчером."
        className={styles.emptyList}
      />
    );
  }

  return (
    <div ref={listRef} className={styles.list}>
      <AutoSizer disableHeight>
        {({ width }) => (
          <InfiniteLoader
            isRowLoaded={isRowLoaded}
            loadMoreRows={loadMoreRows}
            rowCount={totalElements}
          >
            {({ onRowsRendered }) => (
              <VirtualizedList
                rowCount={totalElements}
                deferredMeasurementCache={cache}
                rowHeight={cache.rowHeight}
                rowRenderer={rowRenderer}
                width={width}
                height={listRef.current?.offsetHeight || 500}
                onRowsRendered={onRowsRendered}
                overscanRowCount={10}
              />
            )}
          </InfiniteLoader>
        )}
      </AutoSizer>
    </div>
  );
};

const MyTrips: FC = observer(() => {
  const navigate = useNavigate();
  const { type } = useParams<{ type: string }>();

  const { mapStore } = useAppStore();

  const handleChangeTab = (value: string) => {
    navigate(`${routes.MyTrips}/${value}`);
  };

  const onBack = () => {
    navigate(routes.Nav);
  };

  return (
    <div className={styles.container}>
      <NavBar onBack={onBack}>Мои заказы</NavBar>

      <Tabs
        activeKey={type}
        className={styles.tabs}
        onChange={handleChangeTab}
      >
        <Tabs.Tab
          title="В работе"
          key={TripTabs.Active}
          destroyOnClose
        >
          <PassTrips mapCenter={mapStore.center} />
        </Tabs.Tab>
        <Tabs.Tab
          title="Завершенные"
          key={TripTabs.InActive}
          destroyOnClose
        >
          <PassTrips mapCenter={mapStore.center} />
        </Tabs.Tab>
      </Tabs>
    </div>
  );
});

export default MyTrips;
