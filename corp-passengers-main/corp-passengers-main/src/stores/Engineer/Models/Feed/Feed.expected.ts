import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Coordinate = t.partial({
  latitude: t.number,
  longitude: t.number,
});

export type Coordinate = t.TypeOf<typeof Coordinate>;

export const Segment = t.partial({
  cost: t.number,
  distance: t.number,
  time: t.number,
  coordinates: t.array(Coordinate),
});

export type Segment = t.TypeOf<typeof Segment>;

export const Waypoint = t.partial({
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: tt.nullable(t.string),
  structure: tt.nullable(t.string),
  latitude: tt.nullable(t.number),
  longitude: tt.nullable(t.number),
  checkinAutomatic: tt.nullable(t.boolean),
  checkinManual: tt.nullable(t.boolean),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
});

export const Expected = t.intersection([
  t.type({
    waypoints: tt.nullable(t.array(Waypoint)),
  }),
  t.partial({
    waypointsCount: tt.nullable(t.number),
    waypointsCountWithCheckIn: tt.nullable(t.number),
    waypointsCountWithoutCheckIn: tt.nullable(t.number),
    cost: tt.nullable(tt.money),
    distance: tt.nullable(t.number),
    segments: tt.nullable(t.array(Segment)),
    time: tt.nullable(t.union([t.string, t.number])),
  }),
]);

export type Expected = t.TypeOf<typeof Expected>;
export type Waypoint = t.TypeOf<typeof Waypoint>;
