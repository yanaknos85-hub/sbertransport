import {
  useMutation, useQuery, useQueryClient, useSuspenseQuery, useInfiniteQuery,
  UseInfiniteQueryOptions
} from '@tanstack/react-query';
import dayjs from 'dayjs';
import {
  Busyness, BusynessFilters, CheckinData, CheckinInfo, DriverTripsFilters, PassTrip, PassTrips,
  ChangeTripStatusData,
  TripData
} from './Trips.types';
import { BaseQueryOptions, BaseSuspenseQueryOptions } from 'api/query';
import { useTripsService } from './Trips';
import { DispatcherRoomKeys, useProfile } from '../DispatcherRoom/DispatcherRoom.query';
import { DriverSelf } from '../DispatcherRoom/DispatcherRoom.types';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { UUID } from 'utils/io-ts';
import useWebsocket from 'api/wesocket';

export enum TripsKeys {
  Trips = 'trips',
  Trip = 'trip',
  Busyness = 'busyness',
  CurrentTrip = 'currentTrip',
  CheckinInfo = 'checkinInfo',
}

declare module 'api' {
  interface Cache {
    trips: {
      key: [typeof TripsKeys.Trips, DriverTripsFilters];
      value: PassTrips;
    };
    trip: {
      key: [typeof TripsKeys.Trips, TripData];
      value: PassTrip;
    };
    busyness: {
      key: [typeof TripsKeys.Busyness, BusynessFilters];
      value: Busyness;
    };
    currentTrip: {
      key: [typeof TripsKeys.CurrentTrip];
      value: Busyness;
    };
    checkinInfo: {
      key: [typeof TripsKeys.CheckinInfo, CheckinData];
      value: CheckinInfo;
    };
  }
}

/** Получение пагинированного списка поездок */
export const useMyTrips = (params: DriverTripsFilters, config?: BaseSuspenseQueryOptions<PassTrips>) => {
  const { getDriverTrips } = useTripsService();
  const { id, contractorId } = useProfile().data;

  return useSuspenseQuery({
    queryKey: [TripsKeys.Trips, params],
    queryFn: () => getDriverTrips({
      driverId: id, contractorId, ...params,
    }),
    ...config,
  });
};

/** Получение пагинированного списка поездок */
export const useInfiniteTripList = (
  params: DriverTripsFilters,
  config?: UseInfiniteQueryOptions<PassTrips, Error, PassTrips, number>
) => {
  const { getDriverTrips } = useTripsService();
  const { id, contractorId } = useProfile().data;

  return useInfiniteQuery({
    queryKey: [TripsKeys.Trips, params],
    queryFn: ({ pageParam = 0 }) => getDriverTrips({
      driverId: id, contractorId, ...params, page: pageParam,
    }),
    getNextPageParam: (lastPage, allPages) => {
      return lastPage.last ? undefined : allPages.length;
    },
    // @ts-ignore
    initialPageParam: 0,
    staleTime: Infinity,
    cacheTime: Infinity,
    ...config,
  });
};

/** Получение подробной информации о поездке */
export const useTrip = (tripId: UUID, config?: BaseSuspenseQueryOptions<PassTrip>) => {
  const { getTrip } = useTripsService();
  const { id, contractorId } = useProfile().data;

  const data = {
    contractorId, driverId: id, tripId,
  };

  return useSuspenseQuery({
    queryKey: [TripsKeys.Trips, data],
    queryFn: () => getTrip({
      driverId: id, contractorId, tripId,
    }),
    ...config,
  });
};

/** Вывод водителя на линию/с линии */
export const useUpdateDriverStatus = () => {
  const { updateDriverStatus } = useTripsService();
  const client = useQueryClient();

  return useMutation({
    mutationFn: updateDriverStatus,
    onSuccess: (data, { state: online }) => {
      client.setQueryData(
        [DispatcherRoomKeys.Profile],
        (prev?: DriverSelf) => prev ? ({ ...prev, online }) : undefined
      );
    },
  });
};

/** Получение списка плановых поездок */
export const useBusyness = (
  data: Omit<BusynessFilters, 'onlyPlanning' | 'driverIds'>,
  config?: BaseQueryOptions<Busyness>
) => {
  const { id } = useProfile().data;
  const { getBusyness } = useTripsService();

  const filters = {
    ...data, driverIds: [id], onlyPlanning: true,
  };

  return useQuery({
    queryKey: [TripsKeys.Busyness, filters],
    queryFn: () => getBusyness({
      ...data, driverIds: [id], onlyPlanning: true,
    }),
    ...config,
  });
};

/** Получение текущей поездки */
export const useCurrentTrip = (config?: BaseQueryOptions<PassTrip>) => {
  const { getCurrentTrip } = useTripsService();
  const { id, contractorId } = useProfile().data;

  return useQuery({
    queryKey: [TripsKeys.CurrentTrip],
    queryFn: () => getCurrentTrip({ driverId: id, contractorId }),
    retry: 0,
    cacheTime: 5 * 60 * 1000,
    staleTime: 5 * 60 * 1000,
    ...config,
  });
};

/** Изменить статус поездки */
export const useChangeTripStatus = () => {
  const { patchTrip } = useTripsService();
  const { contractorId } = useProfile().data;
  const client = useQueryClient();

  return useMutation({
    mutationFn: (data: ChangeTripStatusData) => patchTrip({
      contractorId,
      tripId: data.tripId,
      changes: [
        {
          field: 'status',
          value: data.status,
        },
        {
          field: 'changedByDriver',
          value: true,
        },
        {
          field: 'latitude',
          value: data.latitude,
        },
        {
          field: 'longitude',
          value: data.longitude,
        },
        {
          field: 'timezone',
          value: dayjs().format('[GMT]Z'),
        },
        {
          field: 'type',
          value: data.checkinType,
        },
        {
          field: 'dateTime',
          value: dayjs.utc().format('YYYY-MM-DDTHH:mm:ss[Z]'),
        },
      ],
    }),
    onSuccess: (_, variables) => {
      // Если поездка не новая, то вручную добавляем новый чекин
      if (variables.status !== TRIP_STATUSES.DRIVER_ON_THE_WAY) {
        client.setQueryData(
          [TripsKeys.CheckinInfo, { contractorId, tripId: variables.tripId }],
          (prev?: CheckinInfo) => prev && {
            ...prev,
            chekins: [
              ...prev.chekins,
              {
                longitude: variables.longitude,
                latitude: variables.latitude,
                time: dayjs().format(),
                status: variables.status,
                type: variables.checkinType,
              },
            ],
          }
        );
      }
    },
  });
};

/** Получение чекинов поездки */
export const useCheckinInfo = (
  { tripId }: Omit<CheckinData, 'contractorId'>,
  config?: BaseQueryOptions<CheckinInfo>
) => {
  const { getChekinInfo } = useTripsService();
  const { contractorId } = useProfile().data;

  return useQuery({
    queryKey: [TripsKeys.CheckinInfo, { tripId, contractorId }],
    queryFn: () => getChekinInfo({ tripId, contractorId }),
    cacheTime: Infinity,
    staleTime: Infinity,
    ...config,
  });
};

export const useTripsWebsocket = () => {
  const { getWebsocketUrl } = useTripsService();

  return useWebsocket<PassTrip>(getWebsocketUrl(), PassTrip);
};
