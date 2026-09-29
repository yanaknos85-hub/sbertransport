import { FilterRequest } from './types';

export const FIELD_LABELS: Record<keyof FilterRequest, string> = {
  humanReadableId: 'Номер заявки',
  statusSet: 'Статус поездки',
  organizations: 'Организации',
  approvalDateRange: 'Дата согласования',
  department1: 'Подразделение 1 уровня',
  department2: 'Подразделение 2 уровня',
  department3: 'Подразделение 3 уровня',
  department4: 'Подразделение 4 уровня',
  department5: 'Подразделение 5 уровня',
  department6: 'Подразделение 6 уровня',
  sortSetting: 'Настройки сортировки',
  empty: '',
};

export const TaskStatuses = {
  WAIT: 'В очереди',
  IN_PROGRESS: 'В процессе',
  DONE: 'Завершена',
  CANCELED: 'Отменена',
  ERROR: 'Ошибка',
} as const;
