import * as t from 'io-ts';

export enum WaypointType {
  LOAD = 'LOAD',
  UNLOAD = 'UNLOAD',
}

const Contact = t.array(
  t.type({
    id: t.string,
    mobilePhone: t.string,
    employeeId: t.string,
    fullName: t.string,
  })
);

export type ContactType = t.TypeOf<typeof Contact>;
const OrderWaypoint = t.intersection([
  t.type({
    type: t.union([t.literal(WaypointType.LOAD), t.literal(WaypointType.UNLOAD)]),
    id: t.string,
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    latitude: t.number,
    longitude: t.number,
    _addressString: t.string,
    _regionData: t.string,
    organization: t.string,
    contacts: Contact,
  }),
  t.partial({
    addressString: t.string,
    addressStringRepresentation: t.string,
  }),
]);

export type OrderWaypointType = t.TypeOf<typeof OrderWaypoint>;


const RouteAddress = t.type({
  id: t.string,
  latitude: t.number,
  longitude: t.number,
  country: t.string,
  region: t.string,
  city: t.string,
  street: t.string,
  house: t.string,
  existInVspGosbTbRegistry: t.boolean,
});

export type RouteAddressType = t.TypeOf<typeof RouteAddress>;

const RequestPoints = t.record(t.string, t.string);

export const RouteWaypoint = t.intersection([
  t.type({
    id: t.string,
    type: t.union([t.literal(WaypointType.LOAD), t.literal(WaypointType.UNLOAD)]),
    orderingIndex: t.number,
    address: RouteAddress,
    loader: t.boolean,
    latitude: t.number,
    longitude: t.number,
    weight: t.number,
    volume: t.number,
    distance: t.number,
    requestPoints: RequestPoints,
    organization: t.string,
    contacts: Contact,
    humanReadbleIds: t.array(t.string),
  }),
  t.partial({
    humanReadbleIds: t.array(t.string),
    addressString: t.string,
    addressStringRepresentation: t.string,
    requests: t.any
  }),
]);

export type RouteWaypointType = t.TypeOf<typeof RouteWaypoint>;
