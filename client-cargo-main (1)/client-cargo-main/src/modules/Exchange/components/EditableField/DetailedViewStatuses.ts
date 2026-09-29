import * as t from 'io-ts';

export const CancelType = t.type({
  reason: t.string,
  code: t.string,
});

export type CancelType = t.TypeOf<typeof CancelType>;

export const FinalStatuses = [
  'CARGO_LOST',
  'CARGO_CANCELED',
];

export const AwaitingApprovalStatuses = [
  'CARGO_AWAITING_APPROVAL',
];

export const ApprovedStatuses = [
  'CARGO_APPROVED',
];

export const AwaitingStatuses = [
  'CARGO_AWAITING_DATA',
  'CARGO_DATA_RECEIVED',
  'CARGO_AWAITING_TRANSFER',
  'CARGO_TRANSFER_FINISHED',
  'CARGO_AWAITING_SHIPMENT',
  'CARGO_SHIPMENT_FINISHED',
  'CARGO_AWAITING_DELIVERY_CONFIRMATION',
  'CARGO_TRIAL',
];

export const TripFinishedStatuses = [
  'CARGO_DELIVERY_CONFIRMATION_FINISHED',
];

export const CanceledStatuses = [
  'CARGO_CANCELED',
];

export const DetailViewStatuses = [
  FinalStatuses,
  AwaitingApprovalStatuses,
  ApprovedStatuses,
  AwaitingStatuses,
  TripFinishedStatuses,
  CanceledStatuses,
];
