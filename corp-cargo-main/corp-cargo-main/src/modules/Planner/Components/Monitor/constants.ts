import * as t from 'io-ts';
import { UUID } from 'utils/io-ts';

export const CANCEL_STATUS_CODE = 801;

export const STATUS = 'STATUS';

export interface IStatus<T> {
  name: T;
  rusName: string;
  editable: boolean;
  approvable: boolean;
  cancelable: boolean;
  finalStatus: boolean;
  color: string;
}

export type TKeys<T> = {
  [key in string]: T;
};

enum OrderTitle {
  humanReadableId = 'ID маршрута',
  status = 'Статус маршрута',
  creationTime = 'Дата и время создания',
  departureTime = 'Дата и время отправления',
  shipmentTime = 'Фактическая дата и время доставки',
  author = 'Инициатор',
  contractor = 'Перевозчик',
  contractorName = 'Контрагент',
  driver = 'ФИО водителя',
  driverPhone = 'Мобильный телефон водителя',
  registrationNumber = 'Регистрационный номер автомобиля',
  capacity = 'Грузоподъемность автомобиля, кг',
  autoVolume = 'Объем, м\u00B3',
  applications = 'Заявки',
  additionalInfo = 'Дополнительная информация',
  waitingTime = 'Простой, ч',
  cost = 'Планируемая стоимость, руб',
  actualCost = 'Фактическая стоимость, руб',
  weight = 'Общий вес, кг',
  volume = 'Общий объем, м\u00B3',
  distance = 'Планируемый километраж, км',
  actualDistance = 'Фактический километраж, км',
  creationType = 'Тип создания маршрута',
  loaders = 'Грузчики',
  additionalServices = 'Дополнительные услуги',
  cargoType = 'Вид груза:',
  occupiedPlacesCount = 'Мест:',
  weightOfOne = 'Масса:',
  volumeOfOne = 'Объем:',
  desiredDate = 'Планируемая дата и время погрузки',
  shippingDate = 'Планируемая дата и время доставки',
  factDesiredDate = 'Фактическая дата и время погрузки',
  factShippingDate = 'Фактическая дата и время доставки',
  addressFrom = 'Откуда',
  addressTo = 'Куда',
}
export default OrderTitle;

export enum Statuses {
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_PLANNING_FINISHED = 'CARGO_PLANNING_FINISHED',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export enum CancelReasons {
  MISTAKE = 'mistake',
  CHANGE_DATE_TIME = 'change_date_time',
  CONTRACT_TERMINATION = 'contract_termination',
  OTHER_REASON = 'other_reason',
}

export const StatusNames: TKeys<string> = {
  [Statuses.CARGO_PLANNING]: 'Планирование',
  [Statuses.CARGO_PLANNING_FINISHED]: 'Планирование завершено',
  [Statuses.CARGO_AWAITING_DATA]: 'Отправлено контрагенту',
  [Statuses.CARGO_AWAITING_TRANSFER]: 'На сборе',
  [Statuses.CARGO_TRANSFER_FINISHED]: 'Доставка',
  [Statuses.CARGO_SHIPMENT_FINISHED]: 'Доставлено',
  [Statuses.CARGO_CANCELED]: 'Отменено',
};

export const STATUSES: IStatus<Statuses>[] = [
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
    name: Statuses.CARGO_PLANNING_FINISHED,
    rusName: StatusNames[Statuses.CARGO_PLANNING_FINISHED],
    editable: true,
    approvable: true,
    cancelable: true,
    finalStatus: false,
    color: '#FFB467',
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
    name: Statuses.CARGO_CANCELED,
    rusName: StatusNames[Statuses.CARGO_CANCELED],
    editable: false,
    approvable: false,
    cancelable: false,
    finalStatus: true,
    color: '#FE5B3B',
  },
];

export interface ChangedFieldsProps {
  id: UUID;
  label: string;
  description?: string | number | null;
  name?: string;
  transportType?: string;
  status?: string;
  departureAddressCoordinates?: { latitude: number; longitude: number };
}

export const Fields = t.intersection([
  t.type({
    status: t.string,
  }),
  t.partial({
    vehicleInfo: t.string,
    cancelCode: t.string,
  }),
]);

export type Fields = t.TypeOf<typeof Fields>;

export const CANCEL_ROUTE_STATUSES = [
  StatusNames[Statuses.CARGO_PLANNING],
  StatusNames[Statuses.CARGO_PLANNING_FINISHED],
  StatusNames[Statuses.CARGO_AWAITING_TRANSFER],
  StatusNames[Statuses.CARGO_AWAITING_DATA],
];

const CANCEL_REASONS = {
  [CancelReasons.MISTAKE]: 'Ошибочно создан',
  [CancelReasons.CHANGE_DATE_TIME]: 'Изменились дата/время',
  [CancelReasons.CONTRACT_TERMINATION]: 'Расторжение договора с перевозчиком',
  [CancelReasons.OTHER_REASON]: 'Другая причина',
};

export const CANCEL_ROUTE_OPTIONS = [
  {
    value: CancelReasons.MISTAKE,
    label: CANCEL_REASONS[CancelReasons.MISTAKE],
  },
  {
    value: CancelReasons.CHANGE_DATE_TIME,
    label: CANCEL_REASONS[CancelReasons.CHANGE_DATE_TIME],
  },
  {
    value: CancelReasons.CONTRACT_TERMINATION,
    label: CANCEL_REASONS[CancelReasons.CONTRACT_TERMINATION],
  },
  {
    value: CancelReasons.OTHER_REASON,
    label: CANCEL_REASONS[CancelReasons.OTHER_REASON],
  },
];
