import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import {
  Passenger, RequestRating, TaxiClass, TaxiOptions
} from '../../Trip/Trip.interface';
import { Purpose } from '../../PublicRegistry/models/PublicRegistry.interface';

export const FactDataDTO = t.partial({
  tripStartTime: tt.nullable(t.union([t.string, t.array(t.number)])),
  tripFactPrice: tt.nullable(tt.money),
  tripFactWaitTime: tt.nullable(t.number),
  tripFactDistance: tt.nullable(t.number),
  tripFactDuration: tt.nullable(t.number),
  organizationId: tt.nullable(tt.uuid),
});

export type FactDataDTO = t.TypeOf<typeof FactDataDTO>;

export const TaxiWaypoint = t.partial({
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: tt.nullable(t.string),
  structure: tt.nullable(t.string),
  latitude: tt.nullable(t.number),
  longitude: tt.nullable(t.number),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
  waitTime: tt.nullable(t.union([t.string, t.number])),
  checkinAutomatic: tt.nullable(t.boolean),
  checkinManual: tt.nullable(t.boolean),
  absenceReason: tt.nullable(t.string),
  checkinOnlyManual: tt.nullable(t.boolean),
  active: tt.nullable(t.boolean),
});

export type TaxiWaypoint = t.TypeOf<typeof TaxiWaypoint>;

export const TripStop = t.intersection([
  t.type({
    orderId: tt.nullable(t.union([tt.uuid, t.number])),
  }),
  t.partial({
    requestId: tt.nullable(tt.uuid),
    active: tt.nullable(t.boolean),
    eventType: tt.nullable(t.string),
    startTime: tt.nullable(t.string),
    endTime: tt.nullable(t.string),
    waypoint: tt.nullable(TaxiWaypoint),
  }),
]);

export type TripStop = t.TypeOf<typeof TripStop>;

export const orderKpi = t.type({
  orderId: tt.nullable(t.string),
  costSharePart: tt.nullable(t.number),
  rideTimeMin: tt.nullable(t.number),
  savings: tt.nullable(t.number),
  savingsPct: tt.nullable(t.number),
  orderDistanceKm: tt.nullable(t.number),
});

export type OrderKpi = t.TypeOf<typeof orderKpi>;

export const Kpi = t.type({
  totalCost: tt.nullable(t.number),
  totalDistanceKm: tt.nullable(t.number),
  totalTimeMin: tt.nullable(t.number),
  ordersKpi: tt.nullable(t.array(orderKpi)),
});

export type Kpi = t.TypeOf<typeof Kpi>;

export const MagentaSharedRequest = t.type({
  magentaId: tt.nullable(t.number),
  passengers: tt.nullable(t.number),
  employeePassengers: tt.nullable(t.array(Passenger)),
  tariffId: tt.nullable(tt.uuid),
  active: tt.nullable(t.boolean),
  stops: tt.nullable(t.array(TripStop)),
  kpi: Kpi,
});

export const TaxiTripFactData = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    status: t.string,
    passenger: Passenger,
    creationTime: t.number,
    purpose: Purpose,
    tariffId: tt.uuid,
  }),
  t.partial({
    passengers: t.array(Passenger),
    approvedBy: Passenger,
    factDataDTO: tt.nullable(FactDataDTO),
    waypoints: t.array(TaxiWaypoint),
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass),
    passengerCount: t.number,
    coopTrip: tt.nullable(t.boolean),
    requestOptions: t.array(ioTypeFromEnum<TaxiOptions>('TaxiOptions', TaxiOptions)),
    commentForDriver: t.string,
    requestRating: tt.nullable(RequestRating),
    sharedRideId: t.string,
    magentaSharedRequest: MagentaSharedRequest,
  }),
]);

export type TaxiTripFactData = t.TypeOf<typeof TaxiTripFactData>;
