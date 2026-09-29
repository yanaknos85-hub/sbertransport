// FIXME sonarjs/no-duplicate-string

export const TimelineTaxiStatuses = [
  { statuses: ['На согласовании'] },
  { statuses: ['Согласована'] },
  { statuses: ['Ожидайте назначения водителя'] },
  { statuses: ['Поиск водителя'] },
  { statuses: ['Водитель назначен'] },
  { statuses: ['Водитель в пути'] },
  { statuses: ['Водитель ожидает в точке отправления'] },
  { statuses: ['Поездка началась'] },
  { statuses: ['Поездка завершена', 'Отменено'] },
];

export const TimelinePersonalStatuses = [
  { statuses: ['На согласовании'] },
  { statuses: ['Согласована'] },
  { statuses: ['Согласование присоединения к СП'] },
  { statuses: ['Присоединение не согласовано'] },
  { statuses: ['Поездка началась'] },
  { statuses: ['Утверждение маршрута'] },
  { statuses: ['Формирование приказа на выплату'] },
  { statuses: ['Ожидание выплаты'] },
  { statuses: ['Выплата произведена', 'Выплата не произведена', 'Отменено'] },
];

export const TimelinePublicStatuses = [
  { statuses: ['На согласовании'] },
  { statuses: ['Подтверждение поездки'] },
  { statuses: ['Утверждение'] },
  { statuses: ['Формирование приказа на выплату'] },
  { statuses: ['Ожидание выплаты'] },
  { statuses: ['Выплата произведена', 'Выплата не произведена', 'Отменено'] },
];

export const TimelineCarsharingStatuses = [
  { statuses: ['На согласовании'] },
  { statuses: ['Согласована'] },
  { statuses: ['Не согласована'] },
  { statuses: ['Поездка запланирована'] },
  { statuses: ['Поездка началась'] },
  { statuses: ['Утверждение маршрута'] },
  { statuses: ['Поездка завершена', 'Отменено'] },
];
