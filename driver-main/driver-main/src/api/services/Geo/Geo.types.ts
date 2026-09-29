import { TRANSPORT_SERVICE_TYPES } from 'constants/geo.constants';
import * as t from 'io-ts';

/** Точка на карте */
export const Point = t.type({
  /** Широта */
  latitude: t.number,
  /** Долгота */
  longitude: t.number,
});
export type Point = t.TypeOf<typeof Point>;

export interface RouteData {
  coordinates: Point[];
  transportServiceType: TRANSPORT_SERVICE_TYPES;
}

/** Сегмент маршрута */
export const Segment = t.type({
  /** Длина сегмента */
  distance: t.number,
  /** Время, за которое пройден сегмент */
  time: t.number,
  /** Точки сегмента */
  coordinates: t.array(Point),
});
export type Segment = t.TypeOf<typeof Segment>;

/** Маршрут */
export const Route = t.type({
  /** Расстояние */
  distance: t.number,
  /** Время в пути */
  time: t.number,
  /** Сегменты для построения на карте */
  segments: t.array(Segment),
});
export type Route = t.TypeOf<typeof Route>;

export interface TWaypoint {
  latitude: number;
  longitude: number;
  country: string;
  region: string;
  city: string;
  street: string;
  house: string;
  building: string;
  structure: string;
  place: string;
  livingArea: string;
  settlement: string;
  waitTime: number;
  checkinAutomatic: boolean;
  checkinManual: boolean;
  absenceReason: string;
  icon: string;
  district: string;
  active: boolean;
  name: string;
}

export type LatLngTuple = [number, number];
