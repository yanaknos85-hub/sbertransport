export const TARIFF_SYSTEM_TYPE_LABEL = 'Пользовательский';
export const TARIFF_SYSTEM_TYPE_VALUE = 'TARIFF_SYSTEM_TYPE';

export enum Field {
  externalId = 'externalId',
  requestId = 'requestId',

  errors = 'errors',

  transportType = 'transportType',
  express = 'express',
  cargoType = 'cargoType',
  cargoName = 'cargoName',
  cargoCategory = 'cargoCategory',
  fragile = 'fragile',

  width = 'width',
  length = 'length',
  height = 'height',
  weight = 'weight',
  volume = 'volume',

  occupiedPlacesCount = 'occupiedPlacesCount',

  desiredDate = 'desiredDate',
  desiredTime = 'desiredTime',

  senderAddress = 'senderAddress',
  senderOrganization = 'senderOrganization',
  senderName = 'senderName',
  senderMobilePhone = 'senderMobilePhone',
  sourceLoaders = 'sourceLoaders',

  recipientAddress = 'recipientAddress',
  recipientOrganization = 'recipientOrganization',
  recipientName = 'recipientName',
  recipientMobilePhone = 'recipientMobilePhone',
  destinationLoaders = 'destinationLoaders',
  comment = 'comment',
  requestNumber = 'requestNumber',
  approver = 'approver',
}

export const namedField: Record<string, string> = {
  [Field.externalId]: 'Входящий номер', // какой то номер из XLS

  [Field.errors]: 'Ошибки',
  // не используем в отображении
  [Field.transportType]: 'Тип транспорта',
  [Field.express]: 'Срочность отправки',
  [Field.requestNumber]: 'Номер маршрута',

  // не используем
  [Field.cargoCategory]: 'Категория', // монитор / ноутбук
  [Field.cargoType]: ' Тип груза', // техника / мебель
  [Field.fragile]: 'хрупкий',

  [Field.cargoName]: 'Название груза', // Ipad / Macbook
  [Field.width]: 'Ширина общ., см',
  [Field.length]: 'Длина общ., см',
  [Field.height]: 'Высота общ., см',
  [Field.weight]: 'Вес общ., кг',
  [Field.volume]: 'Объем общ., м³',

  [Field.occupiedPlacesCount]: 'Кол-во мест',

  [Field.desiredDate]: 'Дата отправления',

  [Field.senderAddress]: 'Адрес отправления',
  [Field.senderOrganization]: 'Организация отправитель',
  [Field.senderName]: 'ФИО отправителя',
  [Field.senderMobilePhone]: 'Контактные данные отправителя',
  [Field.sourceLoaders]: 'Грузчики в пункте отправления',

  [Field.recipientAddress]: 'Адрес доставки',
  [Field.recipientOrganization]: 'Организация получатель',
  [Field.recipientName]: 'ФИО получателя',
  [Field.recipientMobilePhone]: 'Контактные данные получателя',
  [Field.approver]: 'Согласующий',
  [Field.destinationLoaders]: 'Грузчики в пункте доставки',
  [Field.comment]: 'Комментарий',
};
