import * as t from 'io-ts';

export type LatLngTuple = [number, number];

export const Contact = t.type({
  id: t.string,
  mobilePhone: t.string,
  employeeId: t.string,
  fullName: t.string,
});

export const IOWaypoint = t.partial({
  country: t.string,
  region: t.string,
  district: t.string,
  city: t.string,
  livingArea: t.string,
  settlement: t.string,
  place: t.string,
  street: t.string,
  house: t.string,
  building: t.string,
  structure: t.string,
  latitude: t.number,
  longitude: t.number,
  waitTime: t.number,
  type: t.string,
  typePoint: t.string,
  organization: t.string,
  contacts: t.array(Contact),
  addressStringRepresentation: t.string,
  checkinAutomatic: t.boolean,
  checkinManual: t.boolean,
  absenceReason: t.string,
  icon: t.string,
  gosb: t.string,
  addressType: t.string,
  // ToDo: kic-нет схемы по этому полю. Исправить как будет
  kic: t.any,
  name: t.string,
  vsp: t.string,
  existInVspGosbTbRegistry: t.boolean,
  orderingIndex: t.number,
});

export type TWaypoint = t.TypeOf<typeof IOWaypoint>;

export const IOKic = t.partial({
  id: t.string,
  name: t.string,
  code: t.string,
  address: IOWaypoint,
});

export const IOWaypointVSP = t.partial({
  id: t.string,
  number: t.string,
  name: t.string,
  headFio: t.string,
  headId: t.string,
  headPhone: t.string,
  gosb: t.string,
  tb: t.string,
  address: IOWaypoint,
  code: t.string,
  existInVspGosbTbRegistry: t.boolean,
  kic: IOKic,
});

export type TWaypointVSP = t.TypeOf<typeof IOWaypointVSP>;

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
    deliveryTime: t.number,
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
