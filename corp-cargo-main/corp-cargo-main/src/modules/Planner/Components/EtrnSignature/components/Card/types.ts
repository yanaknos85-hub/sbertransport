import * as t from 'io-ts';

/**
 * Статусы карточки ЭТрН (Card level)
 */
export const CardStatusCode = t.union([
  t.literal('IDENTIFIED'),
  t.literal('WAIT_KORUS_DATA'),
  t.literal('WAIT_CONDITIONS'),
  t.literal('READY_FOR_BANK_ACTION'),
  t.literal('WAIT_KORUS_CONFIRMATION'),
  t.literal('STAGE_COMPLETED'),
  t.literal('PROCESS_COMPLETED'),
  t.literal('ERROR'),
]);

/**
 * Статусы титула Т3 (Title level)
 */
export const TitleStatusCode = t.union([
  t.literal('EXPECTED'),
  t.literal('AVAILABLE'),
  t.literal('WAIT_CONDITIONS'),
  t.literal('READY_TO_SIGN'),
  t.literal('SIGNING'),
  t.literal('SIGNED_LOCALLY'),
  t.literal('SENT_TO_OPERATOR'),
  t.literal('ACCEPTED_BY_OPERATOR'),
  t.literal('ERROR'),
  t.literal('REJECTED'),
  t.literal('OUTCOME_UNKNOWN'),
]);

/**
 * Состояния signing operation
 */
export const SigningOpStatusCode = t.union([
  t.literal('CREATED'),
  t.literal('SIGNING'),
  t.literal('SIGNED_LOCALLY'),
  t.literal('SENDING_TO_KORUS'),
  t.literal('ACCEPTED_BY_KORUS'),
  t.literal('ERROR'),
  t.literal('REJECTED'),
  t.literal('OUTCOME_UNKNOWN'),
]);

/**
 * Состояния lock
 */
export const LockStatusCode = t.union([
  t.literal('ACTIVE'),
  t.literal('LOCKED_BY_OTHER'),
  t.literal('LOCK_EXPIRED'),
]);

// Тип титула: T1/T2/T3/T4
export const TitleTypeCode = t.union([
  t.literal('T1'),
  t.literal('T2'),
  t.literal('T3'),
  t.literal('T4'),
]);

// Тип этапа
export const StageCode = t.union([
  t.literal('STAGE_1'),
  t.literal('STAGE_2'),
  t.literal('STAGE_3'),
]);

// Состояние условия готовности
export const ConditionStateCode = t.union([
  t.literal('OK'),
  t.literal('WAIT'),
  t.literal('ERROR'),
]);

// ============================================================
// PartyShortVm — участник перевозки
// ============================================================

export const PartyShortVm = t.type({
  id: t.string,
  name: t.string,
  inn: t.union([t.null, t.string]),
  role: t.string,
});

export const WorklistItem = t.type({
  id: t.string,
  etrnDisplayNumber: t.string,
  statusCode: CardStatusCode,
  statusDisplayName: t.string,
  controlDeadline: t.union([t.null, t.string]),
  targetTitle: TitleTypeCode,
  sender: t.union([t.null, t.string]),
  receiver: t.union([t.null, PartyShortVm]),
  carrier: t.union([t.null, PartyShortVm]),
});

export const WorklistResponse = t.type({
  items: t.array(WorklistItem),
  total: t.number,
  page: t.number,
  size: t.number,
});

export const LockDto = t.type({
  state: LockStatusCode,
  token: t.string,
  ownerDisplay: t.union([t.null, t.string]),
  expiresAt: t.string,
  serverTime: t.string,
});

// LockAcquire — тело запроса на установку блокировки
export const LockAcquireRequest = t.type({
  userId: t.string,
  cardId: t.string,
});

// LockRelease — тело запроса на снятие блокировки
export const LockReleaseRequest = t.type({
  token: t.string,
});

export const SignCommand = t.type({
  cardId: t.string,
  titleType: t.literal('T3'),
  lockToken: t.string,
  requestKey: t.string,
});

export const SigningOperationDto = t.type({
  operationId: t.string,
  state: SigningOpStatusCode,
  displayText: t.string,
  reason: t.union([t.null, t.string]),
  correlation: t.union([t.null, t.string]),
  updatedAt: t.string,
});

/** TitleChain — элемент массива titleChain */
export const TitleChainDto = t.type({
  title: t.string,
  signedAt: t.union([t.null, t.string]),
  signedBy: t.union([t.null, t.string]),
});

/** CheckItem — элемент массива checks */
export const CheckItemDto = t.type({
  name: t.string,
  passed: t.boolean,
});

/** Verifications — блок проверок */
export const VerificationsDto = t.type({
  checks: t.array(CheckItemDto),
  overallPassed: t.boolean,
  verifiedAt: t.string,
});

/** LockInfo — вложенный объект блокировки */
export const LockInfoDto = t.type({
  userId: t.string,
  lockUntil: t.string,
});

// EtrnCardDto — полный ответ карточки (контракт GET /api/etrn-signature-cargo/{id})
export const EtrnCardDto = t.type({
  id: t.string,
  humanReadableId: t.string,
  applicationNumber: t.union([t.null, t.string]),
  routeNumber: t.union([t.null, t.string]),
  status: t.string,
  sla: t.union([t.null, t.string]),
  timeZone: t.union([t.null, t.string]),
  currentTitle: t.union([t.null, t.string]),
  senderName: t.union([t.null, t.string]),
  receiverName: t.union([t.null, t.string]),
  carrierName: t.union([t.null, t.string]),
  cargoDescription: t.union([t.null, t.string]),
  cargoPlaces: t.union([t.null, t.number]),
  cargoWeightKg: t.union([t.null, t.number]),
  route: t.union([t.null, t.string]),
  mrpaExpiresAt: t.union([t.null, t.string]),
  cargoLength: t.union([t.null, t.number]),
  cargoWidth: t.union([t.null, t.number]),
  cargoHeight: t.union([t.null, t.number]),
  sesFullName: t.union([t.null, t.string]),
  sesRole: t.union([t.null, t.string]),
  sesEventDatetime: t.union([t.null, t.string]),
  sesEventId: t.union([t.null, t.string]),
  titleChain: t.array(TitleChainDto),
  verifications: t.union([t.null, VerificationsDto]),
  lockInfo: t.union([t.null, LockInfoDto]),
  active: t.boolean,
  createdAt: t.string,
  updatedAt: t.string,
  version: t.number,
});

export type CardStatusCodeType = t.TypeOf<typeof CardStatusCode>;
export type TitleStatusCodeType = t.TypeOf<typeof TitleStatusCode>;
export type SigningOpStatusCodeType = t.TypeOf<typeof SigningOpStatusCode>;
export type LockStatusCodeType = t.TypeOf<typeof LockStatusCode>;
export type TitleType = t.TypeOf<typeof TitleTypeCode>;
export type StageCodeType = t.TypeOf<typeof StageCode>;
export type ConditionStateCodeType = t.TypeOf<typeof ConditionStateCode>;

export type PartyShortVmType = t.TypeOf<typeof PartyShortVm>;
export type WorklistItemType = t.TypeOf<typeof WorklistItem>;
export type WorklistResponseType = t.TypeOf<typeof WorklistResponse>;

export type LockDtoType = t.TypeOf<typeof LockDto>;
export type SignCommandType = t.TypeOf<typeof SignCommand>;
export type SigningOperationDtoType = t.TypeOf<typeof SigningOperationDto>;
export type TitleChainDtoType = t.TypeOf<typeof TitleChainDto>;
export type VerificationsDtoType = t.TypeOf<typeof VerificationsDto>;
export type EtrnCardDtoType = t.TypeOf<typeof EtrnCardDto>;

export const SigningEligibilityResponse = t.type({
  attorneyNumber: t.string,
  issueDate: t.string,
  expiryDate: t.string,
});

export type SigningEligibilityResponseType = t.TypeOf<typeof SigningEligibilityResponse>;
