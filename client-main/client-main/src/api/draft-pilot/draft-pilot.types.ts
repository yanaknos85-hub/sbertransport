import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { ActionStatus, SessionStatus, MessageType } from './draft-pilot.constants';

const SessionStatuses = ioTypeFromEnum<SessionStatus>('SessionStatus', SessionStatus);
const ActionStatuses = ioTypeFromEnum<ActionStatus>('ActionStatus', ActionStatus);
const DraftPilotMessageType = ioTypeFromEnum<MessageType>('MessageType', MessageType);

export const PlaceRef = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    region: t.string,
    district: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    name: t.string,
  }),
]);
export type TPlaceRef = t.TypeOf<typeof PlaceRef>;

export const TripPurposeDto = t.type({
  id: tt.uuid,
  label: t.string,
});
export type TTripPurposeDto = t.TypeOf<typeof TripPurposeDto>;

export const RouteWaypointDto = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    waitTime: t.number,
  }),
]);
export type TRouteWaypointDto = t.TypeOf<typeof RouteWaypointDto>;

export const RouteSegmentDto = t.type({
  distance: t.number,
  time: t.number,
  coordinates: t.array(
    t.type({
      latitude: t.number,
      longitude: t.number,
    })
  ),
});
export type TRouteSegmentDto = t.TypeOf<typeof RouteSegmentDto>;

export const RouteDto = t.partial({
  distance: t.number,
  time: t.number,
  waypoints: t.array(RouteWaypointDto),
  segments: t.array(RouteSegmentDto),
});
export type TRouteDto = t.TypeOf<typeof RouteDto>;

export const TAXI_CLASS = ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum);

export const TariffDto = t.partial({
  id: tt.uuid,
  cost: t.number,
  taxiClass: TAXI_CLASS,
});
export type TTariffDto = t.TypeOf<typeof TariffDto>;

export const CalculationResultsDto = t.partial({
  route: RouteDto,
  tariffs: t.array(TariffDto),
});
export type TCalculationResultsDto = t.TypeOf<typeof CalculationResultsDto>;

export const DraftDto = t.partial({
  pickupPoint: PlaceRef,
  destinationPoint: PlaceRef,
  pickupTime: t.string,
  passengerCount: t.number,
  requestId: tt.uuid,
  humanReadableId: t.string,
  tripPurpose: TripPurposeDto,
  calculationResults: CalculationResultsDto,
  taxiClass: TAXI_CLASS,
});
export type TDraftDto = t.TypeOf<typeof DraftDto>;

export const SessionDto = t.type({
  sessionId: tt.uuid,
  status: SessionStatuses,
});
export type TSessionDto = t.TypeOf<typeof SessionDto>;

export const ApiErrorDto = t.type({
  code: t.string,
  message: t.string,
  traceId: tt.uuid,
  details: t.union([t.UnknownRecord, t.undefined]),
});
export type TApiErrorDto = t.TypeOf<typeof ApiErrorDto>;

export const AiMessageToUserRespDto = t.type({
  type: DraftPilotMessageType,
  text: t.string,
});
export type TAiMessageToUserRespDto = t.TypeOf<typeof AiMessageToUserRespDto>;

export const DataResponse = t.type({
  traceId: tt.uuid,
  session: SessionDto,
  draft: tt.nullable(DraftDto),
});
export type TDataResponse = t.TypeOf<typeof DataResponse>;

export const UserMessageRequest = t.intersection([
  t.type({
    text: t.string,
  }),
  t.partial({
    sessionId: t.union([tt.uuid, t.null]),
  }),
]);
export type TUserMessageRequest = t.TypeOf<typeof UserMessageRequest>;

export const UserMessageResponse = t.type({
  data: DataResponse,
  message: AiMessageToUserRespDto,
  action: ActionStatuses,
});
export type TUserMessageResponse = t.TypeOf<typeof UserMessageResponse>;
