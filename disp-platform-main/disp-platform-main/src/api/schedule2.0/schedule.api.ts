import { MutationResultPair, QueryConfig } from 'react-query';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { UUID } from 'utils/io-ts';
import * as t from 'io-ts';
import {
  Busyness,
  BusynessFilters,
  DriverStatus,
  DriverStatusFilters,
  Schedule,
  ScheduleFilters,
  ShiftDetailed,
  ShiftsCreateRequest,
  ShiftsCreateResponse,
  ShiftsUpdate,
  Workload,
  WorkloadRequest
} from './schedule.types';
import {
  DRIVER_STATUSES,
  SCHEDULE,
  PASS_VEHICLE_BUSYNESS,
  CARGO_VEHICLE_BUSYNESS,
  SHIFT,
  SHIFTS,
  SHIFT_ROW
} from './schedule.constants';
import { TRIPS_ANALYTICS_WORKLOAD } from 'api/trips/trips.constants';
import { ShiftConflictKeys } from 'api/shift-conflicts/shift-conflicts.api';
import { ignore } from 'utils/utils';

export enum ScheduleKeys {
  Schedule = 'schedule',
  Statuses = 'driver-statuses',
  PassBusyness = 'pass-vehicle-busyness',
  CargoBusyness = 'cargo-vehicle-busyness',
  ShiftDetailed = 'shift-detailed',
}

declare module 'api' {
  interface Cache {
    [ScheduleKeys.Schedule]: {
      key: [ScheduleKeys.Schedule, UUID, ScheduleFilters];
      value: Schedule;
    };
    [ScheduleKeys.Statuses]: {
      key: [ScheduleKeys.Statuses, UUID, DriverStatusFilters];
      value: DriverStatus[];
    };
    [ScheduleKeys.PassBusyness]: {
      key: [ScheduleKeys.PassBusyness, BusynessFilters];
      value: Busyness[];
    };
    [ScheduleKeys.CargoBusyness]: {
      key: [ScheduleKeys.CargoBusyness, BusynessFilters];
      value: Busyness[];
    };
    [ScheduleKeys.ShiftDetailed]: {
      key: [ScheduleKeys.ShiftDetailed, UUID, UUID];
      value: ShiftDetailed;
    };
  }
}

interface ScheduleParams extends ScheduleFilters {
  contractorId: UUID;
  autoparkId?: string;
}

/** Запрос пагинированного графика работы автомобилей */
export const useSchedule = (
  { contractorId, ...params }: ScheduleParams,
  config?: QueryConfig<Schedule>
): APIQueryResult<Schedule> => (
  useAPI(
    [ScheduleKeys.Schedule, contractorId, params],
    ({ http, process }) => {
      return http
        .get<Schedule>(SCHEDULE, {
          urlParams: { contractorId },
          params,
        })
        .then(process.decodeResponseData(Schedule));
    },
    {
      keepPreviousData: true,
      ...config,
    }
  )
);

interface DriverStatusesParams extends DriverStatusFilters {
  contractorId: UUID;
}

/** Запрос статусов водителей на автомобилях */
export const useDriverStatuses = (
  { contractorId, ...filters }: DriverStatusesParams,
  config?: QueryConfig<DriverStatus[]>
): APIQueryResult<DriverStatus[]> => {
  const {
    startDate, endDate, ...body
  } = filters;

  return (
    useAPI(
      [ScheduleKeys.Statuses, contractorId, filters],
      ({ http, process }) => {
        return http
          .post<DriverStatus[]>(DRIVER_STATUSES, body, {
            urlParams: { contractorId },
          })
          .then(process.decodeResponseData(t.array(DriverStatus)));
      },
      {
        keepPreviousData: true,
        ...config,
      }
    )
  );
};

/** Занятость легковых автомобилей */
export const usePassVehicleBusyness = (
  body: BusynessFilters,
  config?: QueryConfig<Busyness[]>
): APIQueryResult<Busyness[]> => (
  useAPI(
    [ScheduleKeys.PassBusyness, body],
    ({ http, process }) => {
      return http
        .post<Busyness[]>(PASS_VEHICLE_BUSYNESS, body)
        .then(process.decodeResponseData(t.array(Busyness)));
    },
    {
      keepPreviousData: true,
      ...config,
    }
  )
);

/** Занятость легковых автомобилей */
export const useCargoVehicleBusyness = (
  body: BusynessFilters,
  config?: QueryConfig<Busyness[]>
): APIQueryResult<Busyness[]> => (
  useAPI(
    [ScheduleKeys.CargoBusyness, body],
    ({ http, process }) => {
      return http
        .post<Busyness[]>(CARGO_VEHICLE_BUSYNESS, body)
        .then(process.decodeResponseData(t.array(Busyness)));
    },
    {
      keepPreviousData: true,
      ...config,
    }
  )
);

// ================ Старое апи, которое не менялось ============================

export const useShift = (
  { contractorId, shiftId }: { contractorId: UUID; shiftId: UUID },
  config?: QueryConfig<ShiftDetailed, Error>
): APIQueryResult<ShiftDetailed, Error> => (
  useAPI(
    [ScheduleKeys.ShiftDetailed, contractorId, shiftId],
    ({ http, process }) => (
      http.get<ShiftDetailed>(SHIFT, { urlParams: { contractorId, shiftId } })
        .then(process.decodeResponseData(ShiftDetailed))
    ),
    config
  )
);

export const useCreateShifts = (
  contractorId: UUID
): MutationResultPair<ShiftsCreateResponse, unknown, ShiftsCreateRequest, unknown> => (
  useAPIMutation(
    ({ http, process }, data: ShiftsCreateRequest) => (
      http
        .post<ShiftsCreateResponse>(SHIFTS, data, { urlParams: { contractorId }, timeout: 60_000 })
        .then(process.decodeResponseData(ShiftsCreateResponse))
    ),
    {
      onSuccess: ({
        logger, result, cache,
      }) => {
        if (result.some(x => 'id' in x)) {
          logger.toMessage('success', 'Запись успешно создана');
        }
        result.forEach((shift, index) => {
          if ('message' in shift) {
            logger.toNotify('error', shift.message, `Смена №${index + 1}`);
          }
        });
        cache.refetchQueries([ScheduleKeys.Schedule]);
        cache.invalidateQueries([ScheduleKeys.Statuses]);
        cache.refetchQueries([ShiftConflictKeys.ShiftConflicts]);
      },
      onError: ({ logger }) => {
        logger.toMessage('error', 'Произошла ошибка при создании одной или нескольких смен');
      },
    }
  )
);

export const useUpdateShift = (
  contractorId: UUID,
  shiftId: UUID
): MutationResultPair<unknown, unknown, ShiftsUpdate, unknown> => (
  useAPIMutation(
    ({ http }, data: ShiftsUpdate) => (
      http
        .put<unknown>(SHIFT, data, { urlParams: { contractorId, shiftId } })
    ),
    {
      onSuccess: ({ cache, logger }) => {
        logger.toMessage('success', 'Запись успешно обновлена');
        cache.refetchQueries([ScheduleKeys.Schedule]);
        cache.refetchQueries([ScheduleKeys.Statuses]);
        cache.refetchQueries([ScheduleKeys.PassBusyness]);
        cache.refetchQueries([ScheduleKeys.CargoBusyness]);
      },
    }
  )
);

export const useDeleteShifts = (
  contractorId: UUID
): MutationResultPair<unknown, unknown, UUID, unknown> => (
  useAPIMutation(({ http }, shiftId) => (
    http.delete<ShiftDetailed>(SHIFT, { urlParams: { contractorId, shiftId } })
  ), {
    onSuccess: ({ cache, logger }) => {
      cache.refetchQueries([ScheduleKeys.Schedule]);
      cache.refetchQueries([ScheduleKeys.Statuses]);
      cache.refetchQueries([ScheduleKeys.PassBusyness]);
      cache.refetchQueries([ScheduleKeys.CargoBusyness]);
      logger.toMessage('info', 'Запись успешно удалена');
    },
  })
);

export const useDeleteRowShifts = (
  contractorId: UUID,
  rowId: UUID
): MutationResultPair<unknown, unknown, string, unknown> => (
  useAPIMutation(({ http }, date: string) => (
    http.delete<ShiftDetailed>(SHIFT_ROW, { data: { date }, urlParams: { contractorId, rowId } })
  ), {
    onSuccess: ({ cache, logger }) => {
      cache.refetchQueries([ScheduleKeys.Schedule]);
      cache.refetchQueries([ScheduleKeys.Statuses]);
      cache.refetchQueries([ScheduleKeys.PassBusyness]);
      cache.refetchQueries([ScheduleKeys.CargoBusyness]);
      logger.toMessage('info', 'Записи успешно удалены');
    },
  })
);

export const useAnalyticsWorkload = (): MutationResultPair<Workload, unknown, WorkloadRequest, unknown> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<Workload>(TRIPS_ANALYTICS_WORKLOAD, data)
      .then(process.decodeResponseData(Workload))
  ), {
    onSuccess: ignore,
  }
  )
);
