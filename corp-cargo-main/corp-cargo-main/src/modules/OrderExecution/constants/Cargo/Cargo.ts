import { IStatus } from 'modules/OrderExecution/interfaces/Cargo/Cargo.interface';
import { TKeys } from '../../interfaces/Orders.types';

export const CANCEL_STATUS_CODE = 801;

// TODO: Выпилить не используемые тайтлы
export enum OrderTitle {
  humanReadableId = 'ID заявки',
  fullName = 'ФИО',
  status = 'Статус заявки',
  author = 'ФИО заявителя',
  authorPhone = 'Телефон заявителя',
  aprover = 'ФИО согласующего',
  sender = 'ФИО отправителя',
  senderPhone = 'Телефон отправителя',
  recipient = 'ФИО получателя',
  recipientPhone = 'Телефон получателя',
  tariffId = 'Тариф',
  date = 'Дата и время',
  creationTime = 'Дата и время создания заявки',
  departureTime = 'Дата и время отправления',
  shipmentTime = 'Дата и время доставки',
  plannedDate = 'Планируемая дата доставки',
  plannedShipmentTime = 'Планируемая дата и время погрузки',
  waitingTime = 'Время ожидания, ч',
  address = 'Адреса',
  senderAddress = 'Адрес отправления',
  recipientAddress = 'Адрес получения',
  cargo = 'Груз',
  cargoTitle = 'Наименования груза',
  cargoTypes = 'Тип груза',
  volume = 'Габариты груза (Ширина+Длина+Высота), м\u00B3',
  weight = 'Вес груза, кг',
  cargoSpace = 'Количество грузовых мест',
  carBrand = 'Марка',
  carModel = 'Модель',
  clientData = 'Участники грузоперевозки',
  initiator = 'Инициатор',
  person = 'Заявитель',
  personName = 'ФИО заявителя',
  personPhone = 'Телефон заявителя',
  additionalServices = 'Дополнительные услуги',
  moverDepartureAddress = 'Грузчики на адресе отправления (количество)',
  moverReceivingAddress = 'Грузчики на адресе получения (количество)',
  calculatedData = 'Расчетные данные',
  totalWeight = 'Общая масса, кг',
  totalVolume = 'Общий объём, м\u00B3',
  plannedDistance = 'Планируемое расстояние, км',
  actualDistance = 'Фактическое расстрояние, км',
  cost = 'Предварительная стоимость, руб',
  plannedCost = 'Планируемая стоимость по тарифу, руб',
  moverCost = 'Стоимость услуг Грузчиков, руб',
  totalCost = 'Общая планируемая стоимость по Заявке (по тарифу+услуги), руб',
  actualCost = 'Фактическая стоимость, руб',
  contractor = 'Исполнитель',
  tariffContractor = 'Контрагент исполнитель по Тарифу',
  car = 'Марка автомобиля',
  carNumber = 'Номер автомобиля',
  driver = 'ФИО водителя(курьера)',
  driverPhone = 'Номер телефона водителя(курьера)',
  comment = 'Комментарий',
  commentOrder = 'Комментарий к заказу',
  commonComment = 'Комментарий заявителя',
  commentEng = 'Комментарий инженера',
  mobilePhone = 'Мобильный телефон',
  department = 'Подразделение',
  approvedBy = 'Согласующий',
  executor = 'Исполнитель',
  delivery = 'Доставка',
  transferTime = 'Дата и время сбора',
  distance = 'Предварительный километраж, км',
  deliveryType = 'Тип доставки',
  courier = 'ФИО курьера',
  route = 'Маршрут',
  loaders = 'Количество грузчиков',
  tariff = 'Выбор тарифа'
}

export enum Statuses {
  CARGO_ACCEPTED = 'CARGO_ACCEPTED',
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_AWAITING_APPROVAL = 'CARGO_AWAITING_APPROVAL',
  CARGO_APPROVED = 'CARGO_APPROVED',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export enum CancelReasons {
  MISTAKE = 'mistake',
  CHANGE_DATE_TIME = 'change_date_time',
  CHANGE_CARGO = 'change_cargo',
  OTHER_REASON = 'other_reason',
}

export const StatusNames: TKeys<string> = {
  [Statuses.CARGO_ACCEPTED]: 'Принята',
  [Statuses.CARGO_PLANNING]: 'Планирование',
  [Statuses.CARGO_AWAITING_APPROVAL]: 'На согласовании',
  [Statuses.CARGO_APPROVED]: 'Согласовано',
  [Statuses.CARGO_AWAITING_DATA]: 'Отправлено контрагенту',
  [Statuses.CARGO_AWAITING_TRANSFER]: 'На сборе',
  [Statuses.CARGO_TRANSFER_FINISHED]: 'Доставка',
  [Statuses.CARGO_SHIPMENT_FINISHED]: 'Доставлено',
  [Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED]: 'Завершено',
  [Statuses.CARGO_CANCELED]: 'Отменено',
};

export const EXCLUDE_STATUSES_ORDERS_EXECUTION = [
  Statuses.CARGO_CANCELED,
  Statuses.CARGO_ACCEPTED,
  Statuses.CARGO_PLANNING,
  Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
];

export const EXCLUDE_STATUSES_TEMPLATE = [
  Statuses.CARGO_CANCELED,
  Statuses.CARGO_ACCEPTED,
  Statuses.CARGO_PLANNING,
  Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
  Statuses.CARGO_AWAITING_DATA,
  Statuses.CARGO_AWAITING_TRANSFER,
  Statuses.CARGO_TRANSFER_FINISHED,
];

export const STATUSES: IStatus<Statuses>[] = [
  {
    name: Statuses.CARGO_ACCEPTED,
    rusName: StatusNames[Statuses.CARGO_ACCEPTED],
    editable: true,
    approvable: false,
    cancelable: true,
    finalStatus: false,
    color: '#FFB467',
  },
  {
    name: Statuses.CARGO_PLANNING,
    rusName: StatusNames[Statuses.CARGO_PLANNING],
    editable: true,
    approvable: true,
    cancelable: true,
    finalStatus: false,
    color: '#FFB467',
  },
  {
    name: Statuses.CARGO_AWAITING_APPROVAL,
    rusName: StatusNames[Statuses.CARGO_AWAITING_APPROVAL],
    editable: true,
    approvable: true,
    cancelable: true,
    finalStatus: false,
    color: '#FFB467',
  },
  {
    name: Statuses.CARGO_APPROVED,
    rusName: StatusNames[Statuses.CARGO_APPROVED],
    editable: true,
    approvable: false,
    cancelable: true,
    finalStatus: false,
    color: '#17D35B',
  },
  {
    name: Statuses.CARGO_AWAITING_DATA,
    rusName: StatusNames[Statuses.CARGO_AWAITING_DATA],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: false,
    color: '#6979F7',
  },
  {
    name: Statuses.CARGO_AWAITING_TRANSFER,
    rusName: StatusNames[Statuses.CARGO_AWAITING_TRANSFER],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: false,
    color: '#6979F7',
  },
  {
    name: Statuses.CARGO_TRANSFER_FINISHED,
    rusName: StatusNames[Statuses.CARGO_TRANSFER_FINISHED],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: false,
    color: '#6979F7',
  },
  {
    name: Statuses.CARGO_SHIPMENT_FINISHED,
    rusName: StatusNames[Statuses.CARGO_SHIPMENT_FINISHED],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: false,
    color: '#17D35B',
  },
  {
    name: Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED,
    rusName: StatusNames[Statuses.CARGO_DELIVERY_CONFIRMATION_FINISHED],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: true,
    color: '#757575',
  },
  {
    name: Statuses.CARGO_CANCELED,
    rusName: StatusNames[Statuses.CARGO_CANCELED],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: true,
    color: '#FE5B3B',
  },
];

export const CANCEL_ORDER_STATUSES = [
  Statuses.CARGO_PLANNING,
  Statuses.CARGO_APPROVED,
  Statuses.CARGO_AWAITING_DATA,
  Statuses.CARGO_AWAITING_APPROVAL,
  Statuses.CARGO_AWAITING_TRANSFER,
];

const CANCEL_REASONS = {
  [CancelReasons.MISTAKE]: 'Ошибочно создана',
  [CancelReasons.CHANGE_DATE_TIME]: 'Изменились дата/время',
  [CancelReasons.CHANGE_CARGO]: 'Изменился груз',
  [CancelReasons.OTHER_REASON]: 'Другая причина',
};

export const CANCEL_ORDER_OPTIONS = [
  {
    value: CancelReasons.MISTAKE,
    label: CANCEL_REASONS[CancelReasons.MISTAKE],
  },
  {
    value: CancelReasons.CHANGE_DATE_TIME,
    label: CANCEL_REASONS[CancelReasons.CHANGE_DATE_TIME],
  },
  {
    value: CancelReasons.CHANGE_CARGO,
    label: CANCEL_REASONS[CancelReasons.CHANGE_CARGO],
  },
  {
    value: CancelReasons.OTHER_REASON,
    label: CANCEL_REASONS[CancelReasons.OTHER_REASON],
  },
];

export const dOrderFields = {
  commentEng: 'COMMENT_ENGINEER',
  desireDate: 'DESIRED_DATE',
  loaders: 'LOADERS',
  calculatedTariff : 'TARIFF',
}

export const DEFAULT_STATUSES = [
  'CARGO_AWAITING_APPROVAL',
  'CARGO_APPROVED',
  'CARGO_AWAITING_DATA',
  'CARGO_AWAITING_TRANSFER',
  'CARGO_TRANSFER_FINISHED',
  'CARGO_SHIPMENT_FINISHED'
];

export const DEFAULT_STATUS_FILTERS = {
  statuses: Object.keys(StatusNames)
    .filter((status) => !EXCLUDE_STATUSES_ORDERS_EXECUTION.includes(status as Statuses))
};

export enum OrderField {
  authorDepartment = 'authorDepartment',
  creationTime = 'creationTime',
  desiredTime = 'desiredTime',
  id = 'id',
  recipientAddress = 'recipientAddress',
  recipientName = 'recipientName',
  requestType = 'requestType',
  senderAddress = 'senderAddress',
  senderName = 'senderName',
  status = 'status',
}

export const OrderTitles: Record<OrderField, string> = {
  [OrderField.authorDepartment]: 'Подразделение',
  [OrderField.creationTime]: 'Дата и время создания заявки',
  [OrderField.desiredTime]: 'Контрольный срок',
  [OrderField.recipientAddress]: 'Адрес получения',
  [OrderField.recipientName]: 'ФИО получателя',
  [OrderField.requestType]: 'Тип заявки',
  [OrderField.senderAddress]: 'Адрес отправления',
  [OrderField.senderName]: 'ФИО отправителя',
  [OrderField.status]: 'Статус заявки',
  [OrderField.id]: 'ID заявки'
}