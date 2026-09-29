import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export type Coordinates = [number, number];

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
    district: tt.nullable(t.string),
    checkinManual: tt.nullable(t.boolean),
    checkinAutomatic: tt.nullable(t.boolean),
    existInVspGosbTbRegistry: tt.nullable(t.boolean),
    waitTime: tt.nullable(t.number),
  }),
]);

export type Waypoint = t.TypeOf<typeof Waypoint>;

export const Segment = t.type({
  distance: t.number,
  time: t.number,
  coordinates: t.array(t.type({ latitude: t.number, longitude: t.number })),
});

export type Segment = t.TypeOf<typeof Segment>;

export const Route = t.intersection([
  t.type({
    distance: t.number,
    time: t.number,
    segments: t.array(Segment),
    waypoints: t.array(Waypoint),
  }),
  t.partial({
    cost: t.number,
  }),
]);

export type Route = t.TypeOf<typeof Route>;

export const FactRoute = t.partial({
  tripFactWaitTime: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
  tripFactPrice: tt.nullable(t.number),
  tripFactDistance: tt.nullable(t.number),
  tripFactDuration: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
  tripStartTime: tt.nullable(t.number),
  factSearchTime: tt.nullable(t.number),
});

export type FactRoute = t.TypeOf<typeof FactRoute>;

export const IOSegment = t.type({
  distance: t.number,
  time: t.number,
  coordinates: t.array(t.type({ latitude: t.number, longitude: t.number })),
});

export type TSegment = t.TypeOf<typeof IOSegment>;

export type LatLngTuple = [number, number];
export interface ILatLngLiteral {
  latitude: number;
  longitude: number;
}

export interface ISegment {
  distance: number;
  time: number;
  coordinates: ILatLngLiteral[];
}

export const addressString = ({
  street, house, city, district,
}: Waypoint): string => [street, house, city, district].filter(Boolean).join(', ');
