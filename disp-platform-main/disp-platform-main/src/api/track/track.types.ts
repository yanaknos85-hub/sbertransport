import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { RouteTypes, SourceTypes } from './track.constants';

/** Точка на карте */
export const Point = t.type({
  /** Широта */
  latitude: t.number,
  /** Долгота */
  longitude: t.number,
});
export type Point = t.TypeOf<typeof Point>;

/** Сегмент маршрута */
export const Segment = t.type({
  /** Длина сегмента */
  distance: t.number,
  /** Время, за которое пройден сегмент */
  time: tt.nullable(t.number),
  /** Точки сегмента */
  points: t.array(Point),
});
export type Segment = t.TypeOf<typeof Segment>;

/** Фактический маршрут */
export const FactRoute = t.partial({
  /** Фактический пробег */
  distance: tt.nullable(t.number),
  /** Фактическое время в пути */
  time: tt.nullable(t.number),
  /** Сегменты для построения на карте */
  segments: tt.nullable(t.array(Segment)),
});
export type FactRoute = t.TypeOf<typeof FactRoute>;

/** Параметры запроса на получение маршрута */
export interface FactRouteQuery {
  /** Идентификатор поездки */
  tripId: UUID;
  /** Тип маршрута */
  routeType: keyof typeof RouteTypes;
  /** Исходный тип */
  sourceType: keyof typeof SourceTypes;
}
