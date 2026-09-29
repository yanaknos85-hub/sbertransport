import { MapOptions, Padding } from '@2gis/mapgl/types';

/** Москва */
export const MOSCOW: [number, number] = [37.647389, 55.751306];
/** Россия */
export const RUSSIA: [number, number] = [76.213389, 61.679028];
export const DEFAULT_CENTER: [number, number] = RUSSIA;
export const DEFAULT_ZOOM = 11;
export const ZOOM_RUSSIA = 3;

export const DEFAULT_OPTIONS: MapOptions = {
  center: DEFAULT_CENTER,
  zoom: DEFAULT_ZOOM,
  enableTrackResize: true,
  lang: 'ru',
};

// отступ в пикселах для fitBounds
export const DEFAULT_PADDING: Padding = {
  top: 20,
  bottom: 20,
  left: 20,
  right: 20,
};
