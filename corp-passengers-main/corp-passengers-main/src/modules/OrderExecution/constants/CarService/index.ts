import { PAGE_SETTINGS_DEFAULT } from '../Filters';

export enum OrderField {
  humanReadableId = 'humanReadableId',
  status = 'status',
  requestStatusSet = 'requestStatusSet',
  type = 'type',
  creationTime = 'creationTime',
  pickupTime = 'pickupTime',
  deadlineTime = 'deadlineTime',
  workOrder = 'workOrder',
  agreementWorkOrder = 'agreementWorkOrder',
  expediency = 'expediency',
  STO = 'STO',
  nameSTO = 'nameSTO',
  addressSTO = 'addressSTO',
  autoTransferAddress = 'autoTransferAddress',
  towTruckDeliveryAddress = 'towTruckDeliveryAddress',
  autoTransferTime = 'autoTransferTime',
  phoneSTO = 'phoneSTO',
  car = 'car',
  carNumber = 'carNumber',
  carBrand = 'carBrand',
  carModel = 'carModel',
  problem = 'problem',
  person = 'person',
  personName = 'personName',
  personPhone = 'personPhone',
  comment = 'comment',
  evacuation = 'evacuation',
  transportType = 'transportType',
  personnelNumber = 'personnelNumber',
  serviceType = 'serviceType',
  serviceTypeReceived = 'serviceTypeReceived',
  organization = 'organization',
  department = 'department',
}

export const OrderTitle: Record<OrderField, string> = {
  [OrderField.humanReadableId]: 'ID заявки',
  [OrderField.status]: 'Статус заявки',
  [OrderField.requestStatusSet]: 'Статус заявки',
  [OrderField.type]: 'Тип',
  [OrderField.creationTime]: 'Дата создания заявки',
  [OrderField.pickupTime]: 'Дата и время выполнения заявки',
  [OrderField.deadlineTime]: 'Контрольный срок',
  [OrderField.workOrder]: 'Заказ-наряд',
  [OrderField.agreementWorkOrder]: 'Согласование Заказ-наряда',
  [OrderField.expediency]: 'Целесообразность проведения работ',
  [OrderField.STO]: 'СТО',
  [OrderField.nameSTO]: 'Название ООО',
  [OrderField.addressSTO]: 'Адрес СТО',
  [OrderField.autoTransferAddress]: 'Адрес передачи авто',
  [OrderField.towTruckDeliveryAddress]: 'Адрес подачи эвакуатора',
  [OrderField.autoTransferTime]: 'Дата и время передачи авто',
  [OrderField.phoneSTO]: 'Телефон',
  [OrderField.car]: 'Автомобиль',
  [OrderField.carNumber]: 'Государственный номер',
  [OrderField.carBrand]: 'Марка',
  [OrderField.carModel]: 'Модель',
  [OrderField.problem]: 'Причина обращения',
  [OrderField.person]: 'Заявитель',
  [OrderField.personName]: 'ФИО заявителя',
  [OrderField.personPhone]: 'Телефон заявителя',
  [OrderField.comment]: 'Комментарий к заказу',
  [OrderField.evacuation]: 'Эвакуатор',
  [OrderField.transportType]: 'Тип транспорта',
  [OrderField.personnelNumber]: 'Табельный номер',
  [OrderField.serviceType]: 'Тип услуги',
  [OrderField.serviceTypeReceived]: 'Тип получения услуги',
  [OrderField.organization]: 'Организация',
  [OrderField.department]: 'Подразделение',
};

export enum ServiceType {
  GO_MYSELF = 'GO_MYSELF',
  FIELD_SERVICE = 'FIELD_SERVICE',
  EVACUATION = 'EVACUATION',
}

export const ServiceTypeNames: Record<ServiceType, string> = {
  [ServiceType.GO_MYSELF]: 'Поеду сам',
  [ServiceType.FIELD_SERVICE]: 'Выездной сервис',
  [ServiceType.EVACUATION]: 'Эвакуатор',
};

export const GeneralFilterFieldsDefault = {
  dispatcherRequest: true,
  pageSetting: PAGE_SETTINGS_DEFAULT,
};

export enum WorkOrderService {
  repair = 'repair',
  maintenance = 'maintenance',
}

export const WorkOrderPriceComments = {
  totalPrice: 'Итоговая сумма Заказ-наряда некорректна;',
  workPrice: 'Итоговая стоимость работ некорректна;',
  detailPrice: 'Итоговая стоимость деталей некорректна;',
  priceWasChanged: 'Сумма отредактирована пользователем',
};

export const MAX_WORKS_PRICE_RUB = 999_999;
export const MAX_PARTS_PRICE_RUB = 9_999_999;
