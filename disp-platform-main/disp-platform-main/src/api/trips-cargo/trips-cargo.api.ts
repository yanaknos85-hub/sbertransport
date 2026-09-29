import { MutationResultPair } from 'react-query';
import { AxiosError } from 'axios';

import { useAPIMutation } from 'api';
import { DriverBusyness, DriverBusynessData } from 'api/trips/trips.types';
import { ignore } from 'utils/utils';
import { CARGO_BUSYNESS, CARGO_DRIVER_ONLINE_SWITCHER } from './trips-cargo.constants';
import { ScheduleKeys } from 'api/schedule2.0/schedule.api';

/* Вывод грузового водителя на линию */
export const useUpdateCargoDriverOnline = ():
MutationResultPair<unknown, AxiosError, { driverId: string }, unknown> => useAPIMutation(
  ({ http }, { driverId }) => http.put(CARGO_DRIVER_ONLINE_SWITCHER, {}, { urlParams: { driverId }, hush: [409] }),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries([ScheduleKeys.Statuses]);
    },
    onError: ({
      error, logger, t,
    }) => {
      if (error.response?.status === 409) {
        logger.toMessage('error', error.response?.data.message ?? t.Drivers.errorUpdateDriverOnline);
      }
    },
  }
);

/** Занятость водителей (грузовые поездки) */
export const useCargoDriverBusyness = ():
MutationResultPair<DriverBusyness, AxiosError, DriverBusynessData, unknown> => (
  useAPIMutation(
    ({ http, process }, data) => (
      http
        .post<DriverBusyness>(CARGO_BUSYNESS, data)
        .then(process.decodeResponseData(DriverBusyness))
    ),
    {
      onSuccess: ignore,
    }
  )
);
