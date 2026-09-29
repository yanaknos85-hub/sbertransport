import {
  APIQueryResult, useAPI, useAPIMutation, useWebsocket
} from 'api';
import { AxiosError } from 'axios';
import {
  CARGO_BUSYNESS,
  CARGO_DRIVERS_LOCATIONS,
  CARGO_DRIVERS_LOCATION_WEBSOCKET,
  CARGO_DRIVER_ONLINE_SWITCHER,
  CARGO_TRIP,
  CARGO_TRIPS_BY_CONTRACTOR,
  CARGO_TRIPS_BY_DISPATCHER,
  CARGO_TRIPS_WEBSOCKET,
  CARGO_TRIPS_STATISTIC,
  CARGO_TRIPS_KEY,
  CARGO_DRIVERS_LOCATIONS_KEY,
  CARGO_ALL_DRIVERS_LOCATIONS_KEY,
  CARGO_TRIPS_STATISTIC_KEY,
  DRIVERS_CACHE_TIME,
  ALL_DRIVERS_PAGE_SIZE
} from './trips-cargo.constants';
import { useEffect, useRef, useState } from 'react';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  TripsFilters,
  CargoTrips,
  CargoTrip,
  UseDriversLocationsParams,
  DriversLocations,
  SearchDriverParams,
  SetDriversData,
  DriversLocation,
  EditCargoTripData,
  DriverBusyness,
  DriverBusynessData,
  TripsStatistic
} from './trips-cargo.types';
import { UUID } from 'utils/io-ts';
import * as t from 'io-ts';
import { ignore } from 'utils/utils';
import { useAppStore } from 'ioc';
import { PaginationParams } from 'utils/io-ts/pagination';

declare module 'api' {
  interface Cache {
    cargoTrips: {
      key: [typeof CARGO_TRIPS_KEY, UUID, UUID | undefined, TripsFilters];
      value: CargoTrips;
    };
    cargoDriversLocations: {
      key: [typeof CARGO_DRIVERS_LOCATIONS_KEY, UseDriversLocationsParams['query']];
      value: DriversLocations;
    };
    cargoAllDriversLocations: {
      key: [typeof CARGO_ALL_DRIVERS_LOCATIONS_KEY, SearchDriverParams];
      value: DriversLocations[];
    };
    cargoTripStatistic: {
      key: [typeof CARGO_TRIPS_STATISTIC_KEY, UUID];
      value: TripsStatistic;
    };
  }
}

/** Пагинированный список грузовых поездок */
export const useCargoTrips = (
  {
    contractorId,
    dispatcherId,
    query = { page: 0, size: 10 },
  }: {
    contractorId: UUID;
    dispatcherId?: UUID;
    query?: TripsFilters;
  },
  config?: QueryConfig<CargoTrips>
): APIQueryResult<CargoTrips> => useAPI(
  [CARGO_TRIPS_KEY, contractorId, dispatcherId, query],
  ({ http, process }) => http
    .get<CargoTrips>(dispatcherId ? CARGO_TRIPS_BY_DISPATCHER : CARGO_TRIPS_BY_CONTRACTOR, {
      urlParams: { contractorId, ...(dispatcherId && { dispatcherId }) },
      params: query,
    })
    .then(process.decodeResponseData(CargoTrips)),
  config
);

/** Пагинированный список грузовых поездок */
export const useCargoTripsMutation = (
  contractorId: UUID
): MutationResultPair<CargoTrips, AxiosError, TripsFilters & { dispatcherId?: UUID }, unknown> => useAPIMutation(
  ({ http, process }, { dispatcherId, ...filters }) => http
    .get<CargoTrips>(dispatcherId ? CARGO_TRIPS_BY_DISPATCHER : CARGO_TRIPS_BY_CONTRACTOR, {
      urlParams: { contractorId, ...(dispatcherId && { dispatcherId }) },
      params: filters,
    })
    .then(process.decodeResponseData(CargoTrips)),
  {
    onSuccess: ignore,
  }
);

/** Взятие диспетчером в работу грузовых поездки */
export const useAssignCargoTripToDispatcher = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, { tripId: UUID }, unknown> => useAPIMutation(
  ({ http }, { tripId }) => http.patch(CARGO_TRIP, [], {
    urlParams: { contractorId, tripId },
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([CARGO_TRIPS_KEY]);
      cache.refetchQueries([CARGO_TRIPS_STATISTIC_KEY]);
    },
  }
);

/** Редактирование грузовой поездки */
export const useEditCargoTrip = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, { tripId: UUID; data: EditCargoTripData }, unknown> => useAPIMutation(
  ({ http }, { tripId, data }) => http.patch(CARGO_TRIP, data, {
    urlParams: { contractorId, tripId },
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([CARGO_TRIPS_KEY]);
      cache.refetchQueries([CARGO_TRIPS_STATISTIC_KEY]);
    },
  }
);

/** Получение пагинированного списка грузовых водителей по переданным координатам */
export const useCargoDriversLocations = ({
  contractorId,
  query,
}: UseDriversLocationsParams): APIQueryResult<DriversLocations, Error> => useAPI(
  [CARGO_DRIVERS_LOCATIONS_KEY, query],
  ({ http, process }) => http
    .get<DriversLocations>(CARGO_DRIVERS_LOCATIONS, {
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

/** Получение пагинированного списка грузовых водителей по переданным координатам */
export const useCargoDriversLocationsMutation = (
  contractorId: UUID
): MutationResultPair<DriversLocations, AxiosError, UseDriversLocationsParams['query'], unknown> => useAPIMutation(
  ({ http, process }, query) => http
    .get<DriversLocations>(CARGO_DRIVERS_LOCATIONS, {
      urlParams: { contractorId },
      params: query,
    })
    .then(process.decodeResponseData(DriversLocations)),
  {
    onSuccess: ignore,
  }
);

/** Получение списка всех доступных водителей для карты (последовательно запрашивает все страницы) */
export const useAllCargoAvaliableDrivers = (contractorId: UUID, query: SearchDriverParams) => {
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

  const [getAvaliableDrivers] = useCargoDriversLocationsMutation(contractorId);

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

/** Назначение водителя на грузовую поездку */
export const useSetDriverToCargoRequest = (
  contractorId: UUID
): MutationResultPair<unknown, AxiosError, SetDriversData, unknown> => useAPIMutation(
  ({ http }, {
    tripId, driverId, planningShiftId,
  }) => http.patch(CARGO_TRIP, [{
    field:
      planningShiftId ? 'planningShiftId' : 'driverId',
    value: planningShiftId ?? driverId,
  }], {
    urlParams: { contractorId, tripId },
  }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([CARGO_TRIPS_KEY]);
      cache.refetchQueries([CARGO_TRIPS_STATISTIC_KEY]);
    },
  }
);

/* Вывод грузового водителя на линию */
export const useUpdateCargoDriverOnline = (): MutationResultPair<
  unknown,
  AxiosError,
  { driverId: string },
  unknown
> => (
  useAPIMutation(({ http }, { driverId }) => (
    http.put(CARGO_DRIVER_ONLINE_SWITCHER, {}, { urlParams: { driverId }, hush: [409] })
  ), {
    onSuccess: ignore,
    onError: ({
      error, logger, t,
    }) => {
      if (error.response?.status === 409) {
        logger.toMessage('error', error.response?.data.message ?? t.Drivers.errorUpdateDriverOnline);
      }
    },
  })
);

/** Занятость водителей (грузовые поездки) */
export const useCargoDriverBusyness = (): MutationResultPair<
  DriverBusyness[],
  AxiosError,
  DriverBusynessData,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<DriverBusyness[]>(CARGO_BUSYNESS, data)
      .then(process.decodeResponseData(t.array(DriverBusyness)))
  ), {
    onSuccess: ignore,
  }
  )
);

/** Получение обновлений грузовых поездок по вебсокетам */
export const useCargoTripsWebsocket = () => useWebsocket<CargoTrip>(CARGO_TRIPS_WEBSOCKET, CargoTrip);

/** Получение изменений координат грузовых водителей по вебсокетам */
export const useCargoDriversLocationWebsocket = () => useWebsocket<DriversLocation>(
  CARGO_DRIVERS_LOCATION_WEBSOCKET,
  DriversLocation
);

/** Получение статистики */
export const useCargoTripStatistic = (
  contractorId: UUID,
  config?: QueryConfig<TripsStatistic, Error>
): APIQueryResult<TripsStatistic, Error> => (
  useAPI([CARGO_TRIPS_STATISTIC_KEY, contractorId], ({ http, process }) => (
    http
      .get<TripsStatistic>(CARGO_TRIPS_STATISTIC, {
        urlParams: { contractorId },
      })
      .then(process.decodeResponseData(TripsStatistic))
      .catch(ignore)
  ), config
  )
);
