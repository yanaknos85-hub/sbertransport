import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';

const SettingPare = t.type({
  unit: t.union([t.literal('MINUTES'), t.literal('HOURS'), t.literal('DAYS')]),
  value: t.number,
});

export const DeadlineSettings = t.strict({
  id: tt.optional(tt.uuid),
  organizationId: t.string,
  taxiAwaitingApprovalDeadline: SettingPare,
  taxiAwaitingSearchDeadline: SettingPare,
  taxiTripFinishedDeadline: SettingPare,
  personalAwaitingApprovalDeadline: SettingPare,
  personalAwaitingSharedRideApprovalDeadline: SettingPare,
  personalTripInProgressDeadline: SettingPare,
  personalAwaitingTripApprovalDeadline: SettingPare,
  personalOrderPaymentFormationDeadline: SettingPare,
  personalPaymentAwaitingDeadline: SettingPare,
  publicAwaitingApprovalDeadline: SettingPare,
  publicTripConfirmationDeadline: SettingPare,
  publicAwaitingAffirmativeDeadline: SettingPare,
  publicOrderPaymentFormationDeadline: SettingPare,
  publicPaymentAwaitingDeadline: SettingPare,
  employeeLimitDeadline: SettingPare,
  departmentLimitDeadline: SettingPare,
  carsharingJoinDeadline: SettingPare,
  cargoDedicatedAwaitingApprovalDeadline: SettingPare,
  cargoDedicatedApprovedDeadline: SettingPare,
  cargoDedicatedInWorkDeadline: SettingPare,
  cargoDedicatedDeliveryConfirmationDeadline: SettingPare,
  cargoDedicatedTrialDeadline: SettingPare,
  cargoCourierAwaitingApprovalDeadline: SettingPare,
  cargoCourierApprovedDeadline: SettingPare,
  cargoCourierInWorkDeadline: SettingPare,
  cargoCourierDeliveryConfirmationDeadline: SettingPare,
  cargoCourierTrialDeadline: SettingPare,
  cargoInterregionalAwaitingApprovalDeadline: SettingPare,
  cargoInterregionalApprovedDeadline: SettingPare,
  cargoInterregionalInWorkDeadline: SettingPare,
  cargoInterregionalDeliveryConfirmationDeadline: SettingPare,
  cargoInterregionalTrialDeadline: SettingPare,
});

export type DeadlineSettingsType = t.TypeOf<typeof DeadlineSettings>;
