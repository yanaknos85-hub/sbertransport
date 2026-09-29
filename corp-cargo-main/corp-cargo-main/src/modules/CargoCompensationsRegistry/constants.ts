import { Statuses, StatusNames } from './types';
/**
 * Доступные для отображения поля таблицы реестра компенсаций.
 * Их значения служат ключами для i18n.
 */
export enum VisibleFields {
  humanReadableId = 'humanReadableIdVisible', // ID заявки
  routeNumber = 'routeNumberVisible', // Номер маршрута
  courier = 'courierVisible', // ФИО курьера
  department = 'departmentVisible', // Подразделение
  mvz = 'mvzVisible', // МВЗ
  cost = 'costVisible', // Сумма
  status = 'statusVisible', // Статус
  deadline = 'deadlineVisible', // Контрольный срок
  approvedBy = 'approvedByVisible', // Согласующий
  approvalDate = 'approvalDateVisible', // Дата согласования
  formationDate = 'formationDateVisible', // Дата формирования приказа
}

/**
 * Доступные для сортировки поля таблицы реестра.
 * Их значения передаются на backend для сортировки.
 */
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
};

export const statusOptions = [
  { label: StatusNames[Statuses.COMPENSATION_IN_PROCESS], value: Statuses.COMPENSATION_IN_PROCESS },
  { label: StatusNames[Statuses.COMPENSATION_APPROVED], value: Statuses.COMPENSATION_APPROVED },
  { label: StatusNames[Statuses.COMPENSATION_WAITING_FOR_PAYMENT], value: Statuses.COMPENSATION_WAITING_FOR_PAYMENT },
  { label: StatusNames[Statuses.COMPENSATION_COMPLETED], value: Statuses.COMPENSATION_COMPLETED },
  { label: StatusNames[Statuses.COMPENSATION_REJECTED], value: Statuses.COMPENSATION_REJECTED },
];
