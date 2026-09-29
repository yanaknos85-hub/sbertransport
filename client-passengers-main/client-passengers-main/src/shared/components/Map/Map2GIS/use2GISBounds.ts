import {
  LngLatBounds
} from '@sber-sbertransport/ui-kit/src';
import type { Segment } from 'shared/models/geo/types';
import type { WaypointModel } from 'shared/models/geo/Waypoint.model';

interface Props {
  points?: number[][];
  markers?: WaypointModel[];
  polylines?: Segment[];
  carCoordinates?: number[];
  isCarMonitoring?: boolean;
}

const fillBounds = (coordinates: number[][]) => coordinates.reduce((acc: LngLatBounds, point: number[]) => {
  const [longitude, latitude] = point;

  if (!acc.southWest[0] || longitude < acc.southWest[0]) acc.southWest[0] = longitude;
  if (!acc.southWest[1] || latitude < acc.southWest[1]) acc.southWest[1] = latitude;
  if (!acc.northEast[0] || longitude > acc.northEast[0]) acc.northEast[0] = longitude;
  if (!acc.northEast[1] || latitude > acc.northEast[1]) acc.northEast[1] = latitude;

  return acc;
},
{ southWest: [], northEast: [] }
);

export const use2GISBounds = ({
  points,
  markers,
  polylines,
  carCoordinates,
  isCarMonitoring,
}: Props): LngLatBounds | undefined => {
  if (!points && !markers && !polylines) return;

  const coordinates: number[][] = [];
  if (points) coordinates.push(...points);
  if (markers) coordinates.push(...markers.map(marker => ([marker.longitude, marker.latitude])));
  if (polylines) coordinates.push(
    ...polylines
      .map(segment => (segment.coordinates.map(({ longitude, latitude }) => ([longitude as number, latitude as number]))))
      .flat()
  );

  const LONGITUDE_250_METERS = 0.009 * 0.25;
  const LATITUDE_250_METERS_AT_55_DEG_LONGITUDE = 0.015 * 0.25;

  if (coordinates.length === 0) return;

  if (isCarMonitoring && carCoordinates) {
    return fillBounds([carCoordinates, coordinates[0]]);
  }

  if (isCarMonitoring || coordinates.length === 1) {
    const bounds: LngLatBounds = {
      southWest: [],
      northEast: [],
    };
    // Так как bounds имеет приоритет перед zoom, то для 1 точки получается слишком крупный масштаб карты.
    // поэтому для 1 точки используем диапазон +- 250 метров от точки для средней долготы 55 градусов
    // с учетом паддинга карты и размера контейнеров карты это дает итоговый приемлемый масштаб
    const [longitude, latitude] = coordinates[0];
    bounds.southWest[0] = longitude - LONGITUDE_250_METERS;
    bounds.southWest[1] = latitude - LATITUDE_250_METERS_AT_55_DEG_LONGITUDE;
    bounds.northEast[0] = longitude + LONGITUDE_250_METERS;
    bounds.northEast[1] = latitude + LATITUDE_250_METERS_AT_55_DEG_LONGITUDE;

    return bounds;
  }

  return fillBounds(coordinates);
};
