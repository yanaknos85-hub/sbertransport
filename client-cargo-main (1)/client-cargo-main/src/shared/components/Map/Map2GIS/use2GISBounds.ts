import {
  LngLatBounds
} from '@sber-sbertransport/ui-kit/src';
import { Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

export const use2GISBounds = (
  {
    points, markers, polylines,
  }: {
    points?: number[][];
    markers?: WaypointModel[];
    polylines?: Segment[];
  }): LngLatBounds | undefined => {
  if (!points && !markers && !polylines) return;

  const coordinates: number[][] = [];
  if (points) coordinates.push(...points);
  if (markers) coordinates.push(...markers.map(marker => ([marker.longitude, marker.latitude])));
  if (polylines) coordinates.push(
    ...polylines
      .map(segment => (segment.coordinates.map(({ longitude, latitude }) => ([longitude as number, latitude as number]))))
      .flat()
  );

  const bounds: LngLatBounds = {
    southWest: [],
    northEast: [],
  };

  const LONGITUDE_250_METERS = 0.009 * 0.25;
  const LATITUDE_250_METERS_AT_55_DEG_LONGITUDE = 0.015 * 0.25;

  if (coordinates.length === 0) return;

  if (coordinates.length === 1) {
    // Так как bounds имеет приоритет перед zoom, то для 1 точки получается слишком крупный масштаб карты.
    // поэтому для 1 точки используем диапазон +- 250 метров от точки для средней долготы 55 градусов
    // с учетом паддинга карты и размера контейнеров карты это дает итоговый приемлемый масштаб
    const [longitude, latitude] = coordinates[0];
    bounds.southWest[0] = longitude - LONGITUDE_250_METERS;
    bounds.southWest[1] = latitude - LATITUDE_250_METERS_AT_55_DEG_LONGITUDE;
    bounds.northEast[0] = longitude + LONGITUDE_250_METERS;
    bounds.northEast[1] = latitude + LATITUDE_250_METERS_AT_55_DEG_LONGITUDE;
  } else {
    coordinates.forEach(point => {
      const [longitude, latitude] = point;
      if (bounds.southWest[0]) {
        if (longitude < bounds.southWest[0]) bounds.southWest[0] = longitude;
      } else bounds.southWest[0] = longitude;
      if (bounds.southWest[1]) {
        if (latitude < bounds.southWest[1]) bounds.southWest[1] = latitude;
      } else bounds.southWest[1] = latitude;
      if (bounds.northEast[0]) {
        if (longitude > bounds.northEast[0]) bounds.northEast[0] = longitude;
      } else bounds.northEast[0] = longitude;
      if (bounds.northEast[1]) {
        if (latitude > bounds.northEast[1]) bounds.northEast[1] = latitude;
      } else bounds.northEast[1] = latitude;
    });
  }

  return bounds;
};
