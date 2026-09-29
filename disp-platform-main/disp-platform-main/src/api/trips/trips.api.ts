import {
  APIQueryResult, useAPI, useAPIMutation, useWebsocket
} from 'api';
import { AxiosError } from 'axios';
import {
  BUSYNESS,
  CHECKIN_INFO,
  DRIVERS_LOCATIONS,
  DRIVERS_LOCATION_WEBSOCKET,
  DRIVER_ONLINE_SWITCHER,
  TRIP,
  TRIPS_BY_CONTRACTOR,
  TRIPS_BY_DISPATCHER,
  TRIPS_WEBSOCKET,
  TRIPS_STATISTIC
} from './trips.constants';
import { useEffect, useRef, useState } from 'react';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  TripsFilters,
  PassTrips,
  PassTrip,
  UseDriversLocationsParams,
  DriversLocations,
  SetDriversData,
  SearchDriverParams,
  DriversLocation,
  EditTripData,
  CheckinInfo,
  DriverBusyness,
  DriverBusynessData,
  TripsStatistic,
  DriversLocationWebsocket,
  ChangeVehicleData
} from './trips.types';
import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { useAppStore } from 'ioc';
import { PaginationParams } from 'utils/io-ts/pagination';
import { ScheduleKeys } from 'api/schedule2.0/schedule.api';

const DRIVERS_CACHE_TIME = 15 * 1000;
const ALL_DRIVERS_PAGE_SIZE = 20;

export const PASS_TRIPS_KEY = 'pass-trips';
export const PASS_TRIP_KEY = 'pass-trip';
export const DRIVERS_LOCATIONS_KEY = 'drivers-locations';
export const ALL_DRIVERS_LOCATIONS_KEY = 'all-drivers-locations';
export const CHECKIN_INFO_KEY = 'checkin-info';
export const TRIPS_STATISTIC_KEY = 'trips-statistic';

declare module 'api' {
  interface Cache {
    passTrips: {
      key: [typeof PASS_TRIPS_KEY, UUID, UUID | undefined, TripsFilters];
      value: PassTrips;
    };
    passTrip: {
      key: [typeof PASS_TRIP_KEY, UUID, UUID];
      value: PassTrip;
    };
    driversLocations: {
      key: [typeof DRIVERS_LOCATIONS_KEY, UseDriversLocationsParams['query']];
      value: DriversLocations;
    };
    allAriversLocations: {
      key: [typeof ALL_DRIVERS_LOCATIONS_KEY, SearchDriverParams];
      value: DriversLocation[];
    };
    checkinInfo: {
      key: [typeof CHECKIN_INFO_KEY, UUID];
      value: CheckinInfo;
    };
    tripStatistic: {
      key: [typeof TRIPS_STATISTIC_KEY, UUID];
      value: TripsStatistic;
    };
  }
}

/** Пагинированный список пассажирских поездок */
export const usePassTrips = (
  {
    contractorId,
    dispatcherId,
    query = { page: 0, size: 10 },
  }: {
    contractorId: UUID;
    dispatcherId?: UUID;
    query?: TripsFilters;
  },
  config?: QueryConfig<PassTrips>
): APIQueryResult<PassTrips> => useAPI(
  [PASS_TRIPS_KEY, contractorId, dispatcherId, query],
  ({ http, process }) => http
    .get<PassTrips>(dispatcherId ? TRIPS_BY_DISPATCHER : TRIPS_BY_CONTRACTOR, {
      urlParams: { contractorId, ...(dispatcherId && { dispatcherId }) },
      params: query,
    })
    .then(process.decodeResponseData(PassTrips)),
  {
    ...config,
    keepPreviousData: true,
  }
);

/** Пагинированный список пассажирских поездок */
export const usePassTripsMutation = (
  contractorId: UUID
): MutationResultPair<PassTrips, AxiosError, TripsFilters & { dispatcherId?: UUID }, unknown> => useAPIMutation(
  ({ http, process }, { dispatcherId, ...filters }) => http
    .get<PassTrips>(dispatcherId ? TRIPS_BY_DISPATCHER : TRIPS_BY_CONTRACTOR, {
      urlParams: { contractorId, ...(dispatcherId && { dispatcherId }) },
      params: filters,
    })
    .then(process.decodeResponseData(PassTrips)),
  {
    onSuccess: ignore,
  }
);

/** Пассажирская поездка */
export const usePassTrip = (
  { contractorId, tripId }: { contractorId: UUID; tripId: UUID },
  config?: QueryConfig<PassTrip>
): APIQueryResult<PassTrip> => useAPI(
  [PASS_TRIP_KEY, contractorId, tripId],
  ({ http, process }) => http
    .get<PassTrip>(TRIP, { urlParams: { contractorId, tripId } })
    .then(process.decodeResponseData(PassTrip)),
  config
);

/** Взятие диспетчером в работу пассажирской поездки */
export const useAssignTripToDispatcher = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, { tripId: UUID }, unknown> => useAPIMutation(
  ({ http }, { tripId }) => http.patch(TRIP, [{ field: 'dispatcherTakeToWork', value: true }], {
    urlParams: { contractorId, tripId },
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([PASS_TRIPS_KEY]);
      cache.refetchQueries([TRIPS_STATISTIC_KEY]);
    },
  }
);

/** Редактирование пассажирской поездки */
export const useEditTrip = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, { tripId: UUID; data: EditTripData }, unknown> => useAPIMutation(
  ({ http }, { tripId, data }) => http.patch(TRIP, data, {
    urlParams: { contractorId, tripId },
    hush: [409],
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([PASS_TRIPS_KEY]);
      cache.refetchQueries([PASS_TRIP_KEY]);
      cache.refetchQueries([TRIPS_STATISTIC_KEY]);
    },
    onError: ({ error, logger }) => {
      if (error.response?.status === 409) {
        logger.toNotify('error', error.response?.data.message, 'Ошибка');
      }
    },
  }
);

/** Получение пагинированного списка пассажирских водителей по переданным координатам */
export const useDriversLocations = ({
  contractorId,
  query,
}: UseDriversLocationsParams): APIQueryResult<DriversLocations, Error> => useAPI(
  [DRIVERS_LOCATIONS_KEY, query],
  ({ http, process }) => http
    .get<DriversLocations>(DRIVERS_LOCATIONS, {
      urlParams: { contractorId },
      params: query,
    })
    .then(process.decodeResponseData(DriversLocations)),
  {
    enabled: query.latitude && query.longitude,
    cacheTime: DRIVERS_CACHE_TIME,
    staleTime: DRIVERS_CACHE_TIME,
  }
);

/** Получение пагинированного списка пассажирских водителей по переданным координатам */
export const useDriversLocationsMutation = (
  contractorId: UUID
): MutationResultPair<DriversLocations, AxiosError, UseDriversLocationsParams['query'], unknown> => useAPIMutation(
  ({ http, process }, query) => http
    .get<DriversLocations>(DRIVERS_LOCATIONS, {
      urlParams: { contractorId },
      params: query,
    })
    .then(process.decodeResponseData(DriversLocations)),
  {
    onSuccess: ignore,
  }
);

/** Получение списка всех доступных водителей для карты (последовательно запрашивает все страницы) */
export const useAllAvaliableDrivers = (contractorId: UUID, query: SearchDriverParams) => {
  const { logger } = useAppStore();

  const [allDrivers, setAllDrivers] = useState<DriversLocation[]>([]);
  const [fullQuery, setFullQuery] = useState<SearchDriverParams & PaginationParams>({
    ...query,
    page: 0,
    size: ALL_DRIVERS_PAGE_SIZE,
  });

  // Сброс страницы при смене параметров поиска
  const isFirstRender = useRef(true);
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
    } else {
      setFullQuery({
        ...query,
        page: 0,
        size: ALL_DRIVERS_PAGE_SIZE,
      });
    }
  }, [JSON.stringify(query), contractorId]);

  const [getAvaliableDrivers] = useDriversLocationsMutation(contractorId);

  useEffect(() => {
    getAvaliableDrivers(fullQuery)
      .then(data => {
        if (!data) {
          return logger.toMessage('error', 'Не удалось загрузить все автомобили на карте');
        }

        setAllDrivers(prev => fullQuery.page ? [...prev, ...data.content] : data.content);

        if (fullQuery.page < data.totalPages - 1) {
          setFullQuery(prev => ({ ...prev, page: prev.page + 1 }));
        }
      })
      .catch(() => logger.toMessage('error', 'Не удалось загрузить все автомобили на карте'));
  }, [getAvaliableDrivers, fullQuery, logger]);

  return { allDrivers };
};

/** Назначение водителя на пассажирскую поездку */
export const useSetDriverToRequest = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, SetDriversData, unknown> => useAPIMutation(
  ({ http }, {
    tripId, driverId, planningShiftId,
  }) => http.patch(TRIP, [{
    field: planningShiftId ? 'planningShiftId' : 'driverId',
    value: planningShiftId ?? driverId,
  }], {
    urlParams: { contractorId, tripId },
    hush: [409],
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([PASS_TRIPS_KEY]);
      cache.refetchQueries([PASS_TRIP_KEY]);
      cache.refetchQueries([TRIPS_STATISTIC_KEY]);
    },
    onError: ({ error, logger }) => {
      if (error.response?.status === 409) {
        logger.toNotify('error', error.response?.data.message, 'Ошибка смены статуса');
      }
    },
  }
);

/* Вывод пассажирского водителя на линию */
export const useUpdateDriverOnline = (): MutationResultPair<unknown, AxiosError, { driverId: string }, unknown> => (
  useAPIMutation(({ http }, { driverId }) => (
    http.put(DRIVER_ONLINE_SWITCHER, {}, { urlParams: { driverId }, hush: [409] })
  ), {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([ScheduleKeys.Statuses]);
      cache.refetchQueries([ScheduleKeys.Schedule]);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error.response?.status === 409) {
        logger.toMessage('error', error.response?.data.message ?? t.Drivers.errorUpdateDriverOnline);
      }
    },
  })
);

/** Получение чекинов */
export const useCheckinInfoPass = (
  { contractorId, tripId }: { contractorId: UUID; tripId: UUID },
  config?: QueryConfig<CheckinInfo, Error>
): APIQueryResult<CheckinInfo, Error> => useAPI(
  [CHECKIN_INFO_KEY, tripId],
  ({ http, process }) => http
    .get<CheckinInfo>(CHECKIN_INFO, { urlParams: { contractorId, tripId } })
    .then(process.decodeResponseData(CheckinInfo)),
  config
);

/** Занятость водителей (пассажирские поездки) */
export const useDriverBusyness = (): MutationResultPair<DriverBusyness, AxiosError, DriverBusynessData, unknown> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<DriverBusyness>(BUSYNESS, data)
      .then(process.decodeResponseData(DriverBusyness))
  ), {
    onSuccess: ignore,
  }
  )
);

/** Получение обновлений пассажрских поездок по вебсокетам */
export const useTripsWebsocket = () => useWebsocket<PassTrip>(TRIPS_WEBSOCKET, PassTrip);

/** Получение изменений координат пассажирских водителей по вебсокетам */
export const useDriversLocationWebsocket = () => (
  useWebsocket<DriversLocationWebsocket>(DRIVERS_LOCATION_WEBSOCKET, DriversLocationWebsocket)
);

/** Получение статистики */
export const useTripStatistic = (
  contractorId: UUID,
  config?: QueryConfig<TripsStatistic, Error>
): APIQueryResult<TripsStatistic, Error> => (
  useAPI([TRIPS_STATISTIC_KEY, contractorId], ({ http, process }) => (
    http
      .get<TripsStatistic>(TRIPS_STATISTIC, {
        urlParams: { contractorId },
      })
      .then(process.decodeResponseData(TripsStatistic))
      .catch(ignore)
  ), config
  )
);

/** Смена авто на брони */
export const useChangeVehicle = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, ChangeVehicleData, unknown> => useAPIMutation(
  ({ http }, { tripId, vehicleId }) => (
    http.patch(TRIP, [{
      field: 'expectedVehicleId',
      value: vehicleId,
    }], {
      urlParams: { contractorId, tripId },
      hush: [409],
    })
  ),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([PASS_TRIPS_KEY]);
      cache.refetchQueries([PASS_TRIP_KEY]);
      cache.refetchQueries([TRIPS_STATISTIC_KEY]);
    },
    onError: ({ error, logger }) => {
      if (error.response?.status === 409) {
        logger.toNotify('error', error.response?.data.message, 'Ошибка смены автомобиля');
      }
    },
  }
);
