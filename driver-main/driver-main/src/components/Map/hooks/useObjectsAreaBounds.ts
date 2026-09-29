import { LngLatBounds } from '@2gis/mapgl/types';

/**
 * Определяет границы области, в которую помещаются переданные координаты
 * @param {number[][]} coordinates - массив туплов координат [[longitude, latitiude],...]
 * @returns границы области LngLatBounds или undefined
 */
export const useObjectsAreaBounds = (coordinates: number[][]): LngLatBounds | undefined => {
  if (!coordinates || coordinates.length === 0) return;

  const bounds: LngLatBounds = {
    southWest: [],
    northEast: [],
  };
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

  return bounds;
};
