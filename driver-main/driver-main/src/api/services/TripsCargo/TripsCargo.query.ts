import { useMutation, useQueryClient } from '@tanstack/react-query';
import { CargoTrips } from './TripsCargo.types';
import { useTripsCargoService } from './TripsCargo';
import { DispatcherRoomKeys } from '../DispatcherRoom/DispatcherRoom.query';
import { DriverSelf } from '../DispatcherRoom/DispatcherRoom.types';

export enum TripsCargoKeys {
  Trips = 'trips',
}

declare module 'api' {
  interface Cache {
    tripsCargo: {
      key: [typeof TripsCargoKeys.Trips];
      value: CargoTrips;
    };
  }
}

/** Вывод водителя на линию/с линии */
export const useUpdateCargoDriverStatus = () => {
  const { updateDriverStatus } = useTripsCargoService();
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
