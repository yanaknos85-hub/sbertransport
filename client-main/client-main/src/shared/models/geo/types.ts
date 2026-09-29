import * as t from 'io-ts';

export type LatLngTuple = [number, number];

export const IOWaypoint = t.intersection([
  t.type({}),
  t.partial({
    latitude: t.number,
    longitude: t.number,
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    waitTime: t.number,
    checkinAutomatic: t.boolean,
    checkinManual: t.boolean,
    absenceReason: t.string,
    icon: t.string,
  }),
]);

export type TWaypoint = t.TypeOf<typeof IOWaypoint>;

const IOWaypointField = t.intersection([
  IOWaypoint,
  t.type({
    fieldName: t.string,
    addressString: t.string,
    waitingTimeString: t.string,
    isValid: t.boolean,
  }),
]);

export type WaypointField = t.TypeOf<typeof IOWaypointField>;

export const IOCoordinates = t.partial({
  latitude: t.number,
  longitude: t.number,
});

export type TCoordinates = t.TypeOf<typeof IOCoordinates>;

export const Segment = t.type({
  distance: t.number,
  time: t.number,
  coordinates: t.array(IOCoordinates),
});

export type Segment = t.TypeOf<typeof Segment>;

export const RequestRoute = t.intersection([
  t.type({
    distance: t.number,
    time: t.number,
    segments: t.array(Segment),
    waypoints: t.array(IOWaypoint),
  }),
  t.partial({
    cost: t.number,
  }),
]);

export type RequestRoute = t.TypeOf<typeof RequestRoute>;

export const IOGeoZone = t.partial({
  country: t.string,
  region: t.string,
  city: t.string,
  street: t.string,
  house: t.string,
});

export type TGeoZone = t.TypeOf<typeof IOGeoZone>;

export const GeoZoneInfo = t.type({
  id: t.string,
  name: t.string,
  code: t.number,
  parentId: t.number,
});
export type GeoZoneInfo = t.TypeOf<typeof GeoZoneInfo>;
