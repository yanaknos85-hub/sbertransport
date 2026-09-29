import * as t from 'io-ts';

export const cancelCodes = {
  PersonalAndPublicCancelCodes: [
    { reason: 'Отменено пользователем', code: '201' },
    { reason: 'Не согласовано', code: '202' },
    { reason: 'Не согласовано по истечению срока', code: '203' },
    { reason: 'Не утверждено', code: '204' },
    { reason: 'Не утверждено по истечению срока', code: '205' },
    { reason: 'По истечению срока', code: '206' },
  ],
  TaxiCancelCodes: [
    { reason: 'Отменено пользователем', code: '201' },
    { reason: 'Не согласовано', code: '202' },
    { reason: 'Отменено водителем', code: '203' },
  ],
};

export const CancelType = t.type({
  reason: t.string,
  code: t.string,
});

export type CancelType = t.TypeOf<typeof CancelType>;

export const FinalStatuses = [
  'PERSONAL_PAYMENT_DECLINED',
  'PERSONAL_CANCELLED',
  'PUBLIC_PAYMENT_NOT_DONE',
  'PUBLIC_CANCELLED',
  'TAXI_TRIP_FINISHED',
  'TAXI_CANCELLED',
  'CARSHARING_TRIP_FINISHED',
  'CARSHARING_CANCELLED',
  'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  'CARGO_LOST',
  'CARGO_CANCELED',
  'PERSONAL_TRIP_FINISHED',
  'GROUP_TRANSFER_TRIP_FINISHED',
  'GROUP_TRANSFER_CANCELLED',
];

export const AwaitingApprovalStatuses = [
  'TAXI_AWAITING_APPROVAL',
  'PERSONAL_AWAITING_APPROVAL',
  'PUBLIC_AWAITING_APPROVAL',
  'CARSHARING_AWAITING_APPROVAL',
  'CARGO_AWAITING_APPROVAL',
  'GROUP_TRANSFER_AWAITING_APPROVAL',
];

export const ApprovedStatuses = [
  'TAXI_APPROVED',
  'PERSONAL_APPROVED',
  'PUBLIC_TRIP_CONFIRMATION',
  'CARSHARING_APPROVED',
  'CARGO_APPROVED',
  'GROUP_TRANSFER_APPROVED',
];

export const AwaitingStatuses = [
  'TAXI_AWAITING_SEARCH',
  'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  'PUBLIC_AWAITING_AFFIRMATIVE',
  'CARSHARING_AWAITING_SEARCH',
  'CARGO_AWAITING_DATA',
  'CARGO_DATA_RECEIVED',
  'CARGO_AWAITING_TRANSFER',
  'CARGO_TRANSFER_FINISHED',
  'CARGO_AWAITING_SHIPMENT',
  'CARGO_SHIPMENT_FINISHED',
  'CARGO_AWAITING_DELIVERY_CONFIRMATION',
  'CARGO_TRIAL',
  'GROUP_TRANSFER_AWAITING_SEARCH',
];

export const SearchStatuses = ['TAXI_DRIVER_SEARCH', 'GROUP_TRANSFER_DRIVER_SEARCH'];

export const FoundStatuses = ['TAXI_DRIVER_FOUND', 'GROUP_TRANSFER_DRIVER_FOUND'];

export const OnTheWayStatuses = ['TAXI_DRIVER_ON_THE_WAY', 'GROUP_TRANSFER_DRIVER_ON_THE_WAY'];

export const DriverArrivedStatuses = ['TAXI_DRIVER_ARRIVED', 'GROUP_TRANSFER_DRIVER_ARRIVED'];

export const SharedRideDecline = ['PERSONAL_SHARED_RIDE_DECLINED'];

export const TripInProgressStatuses = [
  'TAXI_TRIP_IN_PROGRESS',
  'PERSONAL_TRIP_IN_PROGRESS',
  'CARSHARING_TRIP_IN_PROGRESS',
  'GROUP_TRANSFER_TRIP_IN_PROGRESS',
];

export const AwaitingTripApprovalStatuses = ['PERSONAL_AWAITING_TRIP_APPROVAL'];

export const OrderPaymentFormationStatuses = ['PERSONAL_ORDER_PAYMENT_FORMATION', 'PUBLIC_ORDER_PAYMENT_FORMATION'];

export const PaymentAwaitingStatuses = ['PERSONAL_PAYMENT_AWAITING', 'PUBLIC_PAYMENT_AWAITING'];

export const PaymentDoneStatuses = ['PERSONAL_PAYMENT_DONE', 'PUBLIC_PAYMENT_DONE'];

export const PaymentDeclineStatuses = ['PERSONAL_PAYMENT_DECLINED', 'PUBLIC_PAYMENT_NOT_DONE'];

export const TripFinishedStatuses = [
  'TAXI_TRIP_FINISHED',
  'CARSHARING_TRIP_FINISHED',
  'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  'PERSONAL_TRIP_FINISHED',
  'GROUP_TRANSFER_FINISHED',
  'GROUP_TRANSFER_TRIP_FINISHED',
];

export const CanceledStatuses = [
  'TAXI_CANCELLED',
  'PERSONAL_CANCELLED',
  'PUBLIC_CANCELLED',
  'CARSHARING_CANCELLED',
  'CARGO_CANCELED',
  'GROUP_TRANSFER_CANCELLED',
];

export const DetailViewStatuses = [
  AwaitingApprovalStatuses,
  ApprovedStatuses,
  AwaitingStatuses,
  SearchStatuses,
  FoundStatuses,
  OnTheWayStatuses,
  DriverArrivedStatuses,
  SharedRideDecline,
  TripInProgressStatuses,
  AwaitingTripApprovalStatuses,
  OrderPaymentFormationStatuses,
  PaymentAwaitingStatuses,
  PaymentDoneStatuses,
  PaymentDeclineStatuses,
  TripFinishedStatuses,
  CanceledStatuses,
];
