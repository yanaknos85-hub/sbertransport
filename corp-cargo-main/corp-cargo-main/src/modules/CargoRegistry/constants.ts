/**
 * Fields which cam be shown in register table.
 * Values are also used as i18n keys.
 */
export enum VisibleFields {
  humanReadableId = 'requestIdVisible', // Номер заявки
  status = 'requestStatusVisible', // Статус заявки
  transportType = 'cargoTransportTypeVisible', // Тип тарифа
  author = 'authorVisible', // ФИО заявителя
  authorPhone = 'authorPhoneVisible', // Телефон заявителя
  authorPersonnelNumber = 'authorPersonnelNumberVisible', // Табельный номер
  costCenter = 'costCenterVisible', // МВЗ
  desiredDate = 'desiredDateVisible', // Плановая дата, время сбора
  contractor = 'carrierVisible', // Перевозчик
  expectedCost = 'plannedPriceVisible', // Плановая стоимость
  actualCost = 'actualCostVisible', // Фактическая стоимость
  expectedDistance = 'plannedRangeVisible', // Плановая дальность
  actualDistance = 'actualDistanceVisible', // Фактическая дальность
  plannedDeliveryDate = 'plannedDeliveryDateVisible', // Плановая дата доставки
  transferTime = 'transferTimeVisible', // Фактическая дата сбора
  shipmentTime = 'shipmentTimeVisible', // Фактическая дата доставки
  deadlineDate = 'deadlineDateVisible', // Контрольный срок
  sender = 'senderVisible', // ФИО отправителя
  senderPhone = 'senderPhoneVisible', // Телефон отправителя
  senderAddress = 'waypointFromVisible', // Адрес отправления
  senderOrganization = 'senderOrganizationVisible', // Организация-отправитель
  recipient = 'recipientVisible', // ФИО получателя
  recipientPhone = 'recipientPhoneVisible', // Телефон получателя	
  recipientAddress = 'waypointToVisible', // Адрес назначения	
  recipientOrganization = 'recipientOrganizationVisible', // Организация-получатель	
  waypointsCount = 'waypointsCountVisible', // Количество точек	
  weight = 'weightVisible', // Общий вес
  volume = 'volumeVisible', // 	Объем
  creationDate = 'creationDateVisible', // Дата создания заявки	
  creationTime = 'creationTimeVisible', // Время создания заявки	
  source = 'sourceVisible', // Источник создания	
  routeNumber = 'routeNumberVisible', // Номер маршрута
  templateNumber = 'templateNumberVisible', // Номер расписания
  evaluation = 'evaluationVisible', // Оценка
  economy = 'economyVisible', // Экономия
}

/**
 * Fields which cam be sorted in register table.
 * Values are backend sort parameters.
 */
export const sortFields: { [key in VisibleFields]?: string } = {
  [VisibleFields.humanReadableId]: 'REQUEST_HUMAN_ID',
  [VisibleFields.desiredDate]: 'DESIRED_DATE',
};
