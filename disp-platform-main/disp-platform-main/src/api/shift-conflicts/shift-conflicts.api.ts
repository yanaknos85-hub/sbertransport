import { MutationResultPair, QueryConfig } from 'react-query';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { SHIFT_CONFLICT_ROUTE, SHIFT_CONFLICTS } from './shift-conflicts.constants';
import { DeleteShiftConflictData, ShiftConflicts, ShiftConflictsFilters } from './shift-conflicts.types';

export enum ShiftConflictKeys {
  ShiftConflicts = 'shift-conflicts',
}

declare module 'api' {
  interface Cache {
    [ShiftConflictKeys.ShiftConflicts]: {
      key: [ShiftConflictKeys.ShiftConflicts, ShiftConflictsFilters];
      value: ShiftConflicts;
    };
  }
}

/** Запрос конфликтных смен */
export const useShiftConflicts = (
  filters: ShiftConflictsFilters,
  config?: QueryConfig<ShiftConflicts>
): APIQueryResult<ShiftConflicts> => (
  useAPI(
    [ShiftConflictKeys.ShiftConflicts, filters],
    ({ http, process }) => {
      return http
        .get<ShiftConflicts>(SHIFT_CONFLICTS, { params: filters })
        .then(process.decodeResponseData(ShiftConflicts));
    },
    {
      keepPreviousData: true,
      ...config,
    }
  )
);

/** Удаление конфликтной смены по routeId */
export const useDeleteConflictShift = (): MutationResultPair<unknown, unknown, DeleteShiftConflictData, unknown> => (
  useAPIMutation(({ http }, { routeId }) => (
    http.delete<unknown>(SHIFT_CONFLICT_ROUTE, { urlParams: { routeId } })
  ), {
    onSuccess: ({ cache, logger }) => {
      cache.refetchQueries([ShiftConflictKeys.ShiftConflicts]);
      logger.toMessage('info', 'Конфликтная смена успешно удалена');
    },
  })
);
