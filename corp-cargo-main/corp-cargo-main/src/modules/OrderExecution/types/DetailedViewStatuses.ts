export const AwaitingApprovalStatuses = [
  'TAXI_AWAITING_APPROVAL',
  'PERSONAL_AWAITING_APPROVAL',
  'PUBLIC_AWAITING_APPROVAL',
  'CARSHARING_AWAITING_APPROVAL',
  'CARGO_AWAITING_APPROVAL',
];

export const ApprovedStatuses = [
  'TAXI_APPROVED',
  'PERSONAL_APPROVED',
  'PUBLIC_TRIP_CONFIRMATION',
  'CARSHARING_APPROVED',
  'CARGO_APPROVED',
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
];

export const SearchStatuses = ['TAXI_DRIVER_SEARCH'];

export const FoundStatuses = ['TAXI_DRIVER_FOUND'];

export const OnTheWayStatuses = ['TAXI_DRIVER_ON_THE_WAY'];

export const DriverArrivedStatuses = ['TAXI_DRIVER_ARRIVED'];

export const SharedRideDecline = ['PERSONAL_SHARED_RIDE_DECLINED'];

export const TripInProgressStatuses = [
  'TAXI_TRIP_IN_PROGRESS',
  'PERSONAL_TRIP_IN_PROGRESS',
  'CARSHARING_TRIP_IN_PROGRESS',
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
];

export const CanceledStatuses = [
  'TAXI_CANCELLED',
  'PERSONAL_CANCELLED',
  'PUBLIC_CANCELLED',
  'CARSHARING_CANCELLED',
  'CARGO_CANCELED',
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
