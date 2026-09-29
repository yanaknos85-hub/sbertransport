import * as t from 'io-ts';

export type TKeys<T> = {
  [key in string]: T;
};

export interface IStatus<T> {
  name: T;
  rusName: string;
  editable: boolean;
  approvable: boolean;
  cancelable: boolean;
  finalStatus: boolean;
  color: string;
}

export enum TransportTypes {
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
}

export enum Statuses {
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export const StatusNames: TKeys<string> = {
  [Statuses.CARGO_AWAITING_DATA]: 'Доступна к доставке',
  [Statuses.CARGO_TRANSFER_FINISHED]: 'Доставка',
  [Statuses.CARGO_SHIPMENT_FINISHED]: 'Доставлено',
  [Statuses.CARGO_CANCELED]: 'Отменено',
};

export enum RouteStatusEnum {
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export const STATUSES: IStatus<Statuses>[] = [
  {
    name: Statuses.CARGO_AWAITING_DATA,
    rusName: StatusNames[Statuses.CARGO_AWAITING_DATA],
    editable: true,
    approvable: true,
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
  id: string;
  label: string;
  description?: string | number | null;
  name?: string;
  transportType?: string;
  status?: string;
  departureAddressCoordinates?: { latitude: number; longitude: number };
}

export const TransportStatuses: Record<'cargo', string[]> = {
  cargo: [
    'CARGO_AWAITING_DATA',
    'CARGO_TRANSFER_FINISHED',
    'CARGO_SHIPMENT_FINISHED',
    'CARGO_CANCELED',
  ],
};

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

export const AvailableStatus = t.intersection([
  t.type({
    name: t.string,
    rusName: t.string,
  }),
  t.partial({
    finalStatus: t.boolean,
    color: t.string,
    index: t.number,
  }),
]);

export type AvailableStatus = t.TypeOf<typeof AvailableStatus>;
