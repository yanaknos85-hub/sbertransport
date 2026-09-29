export const TILES = 'tiles';
export const MAP_TILES = `/${TILES}?x={x}&y={y}&z={z}`; // ссылка для проксирования тайлов через sowa
export const DIRECT_TILES_LINK = 'http://tile0.maps.2gis.com/tiles?x={x}&y={y}&z={z}'; // прямая ссылка на 2гис для дев отладки

export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export enum MapComponentType {
  webGL2GIS = 'webGL2GIS',
  leaflet = 'leaflet',
  openlayers = 'openlayers',
}

export const LONGITUDE_250_METERS = 0.009 * 0.25;
export const LATITUDE_250_METERS_AT_55_DEG_LONGITUDE = 0.015 * 0.25;
