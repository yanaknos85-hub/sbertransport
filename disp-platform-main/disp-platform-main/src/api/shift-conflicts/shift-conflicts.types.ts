import * as t from 'io-ts';
import { createPagination, PaginationParams } from 'utils/io-ts/pagination';

import { ShiftConflictReason } from './shift-conflicts.constants';

/** Конфликтная смена */
export const ShiftConflict = t.type({
  /** Идентификатор смены во внешней системе */
  routeId: t.string,

  /** Табельный номер водителя */
  personnelNumber: t.string,

  /** Госномер */
  stateNumber: t.string,

  /** Дата начала смены */
  startDate: t.string,

  /** Дата окончания смены */
  endDate: t.string,

  /** Причина конфликта - название поля с ошибкой */
  conflictReason: t.keyof(ShiftConflictReason),
});
export type ShiftConflict = t.TypeOf<typeof ShiftConflict>;

/** Список конфликтных смен */
export const ShiftConflicts = createPagination(ShiftConflict);
export type ShiftConflicts = t.TypeOf<typeof ShiftConflicts>;

/** Фильтры для запроса конфликтных смен */
export interface ShiftConflictsFilters extends PaginationParams {
  stateNumber?: string;
  personnelNumber?: string;
  conflictReason?: keyof typeof ShiftConflictReason;
}

export interface DeleteShiftConflictData {
  routeId: string;
}
