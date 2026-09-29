import * as t from 'io-ts';

import * as tt from 'utils/io-ts';

export const Waypoint = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: tt.nullable(t.string),
    region: tt.nullable(t.string),
    city: tt.nullable(t.string),
    street: tt.nullable(t.string),
    house: tt.nullable(t.string),
    building: tt.nullable(t.string),
    structure: tt.nullable(t.string),
    checkinManual: tt.nullable(t.boolean),
    checkinAutomatic: tt.nullable(t.boolean),
    existInVspGosbTbRegistry: tt.nullable(t.boolean),
    waitTime: tt.nullable(t.number),
    district: tt.nullable(t.string),
  }),
]);

export type Waypoint = t.TypeOf<typeof Waypoint>;

export const Passenger = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    positionId: tt.nullable(tt.uuid),
    constCenter: tt.nullable(t.string),
    positionName: tt.nullable(t.string),
    mobilePhone: tt.nullable(t.string),
    phone: tt.nullable(t.string),
    userId: tt.nullable(tt.uuid),
    patronymic: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
    mvz: tt.nullable(t.string),
    marriageCertificateNumber: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
    delegatedById: tt.nullable(t.string),
    supervisorId: tt.nullable(t.string),
    organizationId: tt.nullable(tt.uuid),
    departmentId: tt.nullable(tt.uuid),
  }),
]);
export type Passenger = t.TypeOf<typeof Passenger>;

export const Expected = t.type({
  cost: tt.money,
  distance: t.number,
  time: t.number,
  segments: t.array(
    t.type({
      cost: tt.money,
      distance: t.number,
      time: t.number,
      coordinates: t.array(
        t.type({
          latitude: t.number,
          longitude: t.number,
        })
      ),
    })
  ),
  waypoints: t.array(Waypoint),
});

const TGeoPoint = t.strict({
  latitude: t.number,
  longitude: t.number,
});
export const TLastKnownPosition = t.type({
  geoPoint: TGeoPoint,
  pointTime: t.number,
});
export type LastKnownPosition = t.TypeOf<typeof TLastKnownPosition>;

export const RequestDetailsResponse = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: tt.nullable(t.string),
    status: tt.nullable(t.string),
  }),
  t.partial({
    passenger: tt.nullable(Passenger),
    commentForDriver: tt.nullable(t.string),
    departureAddress: tt.nullable(t.string),
    intermediateAddresses: t.array(t.string),
    destinationAddress: tt.nullable(t.string),
    creationTime: tt.nullable(t.number),
    desiredDate: t.number,
    deadlineState: t.string,
    deadline: t.number,
    cost: t.number,
    expected: Expected,
    distance: t.number,
    waitTime: t.number,
    passengerCount: t.number,
    tariffId: tt.uuid,
    taxiClass: t.string,
    contractorId: tt.uuid,
    dateTimeRegistered: t.number,
    factSearchTime: tt.nullable(t.number),
    tripStartTime: tt.nullable(t.number),
    tripFinishTime: tt.nullable(t.number),
    dispatcher: tt.nullable(Passenger),
    resolution: t.string,
    lastKnownPosition: TLastKnownPosition,
  }),
]);
export type RequestDetailsResponse = t.TypeOf<typeof RequestDetailsResponse>;
