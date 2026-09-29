import { LatLngTuple } from 'leaflet';

export function toRad(degree: number): number {
  return (degree * Math.PI) / 180;
}

export function toDeg(radian: number): number {
  return (radian * 180) / Math.PI;
}

/** Получение азимутов по двум GPS-координатам
 * @param p1 LatLngTuple
 * @param p2 LatLngTuple
 *
 * @returns number
 * */
export function getAzimuth(p1: LatLngTuple, p2: LatLngTuple): number {
  const lon1 = toRad(p1[0]);
  const lon2 = toRad(p2[0]);

  const lat1 = toRad(p1[1]);
  const lat2 = toRad(p2[1]);

  const a = Math.sin(lon2 - lon1) * Math.cos(lat2);
  const b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);

  return toDeg(Math.atan2(a, b));
}
