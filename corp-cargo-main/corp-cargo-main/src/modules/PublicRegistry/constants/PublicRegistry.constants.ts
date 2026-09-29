import moment from 'moment';

export const travellingBehavior = [
  { label: 'Да', value: '1' },
  { label: 'Нет', value: '2' },
];

export enum SortFields {
  PASSENGER_FULL_NAME = 'PASSENGER_FULL_NAME',
  CREATION_DATE = 'CREATION_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
}

export const UserSortProperties = {
  SortProperty: 'sortProperty',
  DirectionAsc: 'directionAsc',
};

export const coopTripLabel = 'Совместная';
export const individualTripLabel = 'Индивидуальная';

export const initialFormValues = {
  orderPaymentFormationStartDate: { mode: 'range', value: [moment().startOf('month'), moment()] },
};
