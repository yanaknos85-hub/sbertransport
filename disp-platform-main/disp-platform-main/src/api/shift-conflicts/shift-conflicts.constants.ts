const SHIFT_CONFLICTS_SERVICE = '/dispatcher-room/shift-conflicts';

export const SHIFT_CONFLICTS = `${SHIFT_CONFLICTS_SERVICE}/`;
export const SHIFT_CONFLICT_ROUTE = `${SHIFT_CONFLICTS_SERVICE}/:routeId/`;

/** Причины конфликтов смен */
export enum ShiftConflictReason {
  VEHICLE_NOT_IN_EXPLOITATION = 'VEHICLE_NOT_IN_EXPLOITATION',
  DRIVER_INACTIVE = 'DRIVER_INACTIVE',
  DIFFERENT_AUTOPARKS = 'DIFFERENT_AUTOPARKS',
  DIFFERENT_CONTRACTORS = 'DIFFERENT_CONTRACTORS',
  BOTH_EXISTING_ACTIVE_SHIFT = 'BOTH_EXISTING_ACTIVE_SHIFT',
  DRIVER_EXISTING_ACTIVE_SHIFT = 'DRIVER_EXISTING_ACTIVE_SHIFT',
  VEHICLE_EXISTING_ACTIVE_SHIFT = 'VEHICLE_EXISTING_ACTIVE_SHIFT',
  DRIVER_NOT_FOUND = 'DRIVER_NOT_FOUND',
  VEHICLE_NOT_FOUND = 'VEHICLE_NOT_FOUND',
}

/** Тексты причин конфликтов смен */
export const SHIFT_CONFLICT_REASONS_TEXTS = {
  [ShiftConflictReason.VEHICLE_NOT_IN_EXPLOITATION]: 'Автомобиль не находится в эксплуатации',
  [ShiftConflictReason.DRIVER_INACTIVE]: 'Водитель не является активным',
  [ShiftConflictReason.DIFFERENT_AUTOPARKS]: 'Автомобиль и водитель относятся к разным филиалам',
  [ShiftConflictReason.DIFFERENT_CONTRACTORS]: 'Автомобиль и водитель относятся к разным контрагентам',
  [ShiftConflictReason.BOTH_EXISTING_ACTIVE_SHIFT]: 'На автомобиль и водителя уже существует активная смена',
  [ShiftConflictReason.DRIVER_EXISTING_ACTIVE_SHIFT]: 'На водителя уже существует активная смена',
  [ShiftConflictReason.VEHICLE_EXISTING_ACTIVE_SHIFT]: 'На автомобиль уже существует активная смена',
  [ShiftConflictReason.DRIVER_NOT_FOUND]: 'Водитель не найден',
  [ShiftConflictReason.VEHICLE_NOT_FOUND]: 'Автомобиль не найден',
};
