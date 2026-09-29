import { useState, useEffect } from 'react';
import { Coordinates } from 'stores/Geo/Geo.interface';

const WATCHPOSITION_INIT_OPTIONS = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 5000,
};

export interface IUsePosition {
  position: Coordinates;
  error: string;
}
type Offsets = Record<string, Coordinates>;
export const useGeoPosition = (initialPosition = [0, 0], options = WATCHPOSITION_INIT_OPTIONS): IUsePosition => {
  const [position, setPosition] = useState(initialPosition);
  const [error, setError] = useState('');

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const onChange = ({ coords }: any): void => {
    setPosition([coords.latitude, coords.longitude]);
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const onError = (er: any): void => {
    setError(er.message);
    const d = new Date();
    const timeOffset = d.getTimezoneOffset().toString();
    const offsets: Offsets = {
      '-120': [54.7065, 20.511], // Kaliningrad
      '-180': [55.7522, 37.6156], // Moscow
      '-240': [53.2001, 50.15], // Samara
      '-300': [56.8519, 60.6122], // Ekaterinburg
      '-360': [54.9924, 73.3686], // Omsk
      '-420': [56.0184, 92.8672], // Krasnoyarsk
      '-480': [52.2978, 104.296], // Irkutsk
      '-540': [62.0339, 129.733], // Yakutsk
      '-600': [43.1056, 131.874], // Vladivostok
      '-660': [59.5638, 150.803], // Magadan
      '-720': [53.0444, 158.651], // Petropavlovsk-Kamchatsky
    };
    // eslint-disable-next-line no-prototype-builtins
    if (offsets.hasOwnProperty(timeOffset)) {
      setPosition(offsets[timeOffset]);
    }
  };

  useEffect((): (() => void) => {
    const { geolocation } = navigator;

    if (!geolocation) {
      setError('Geolocation is not supported');

      // eslint-disable-next-line @typescript-eslint/no-empty-function
      return () => {};
    }
    const watcher = geolocation.watchPosition(onChange, onError, options);

    return (): void => geolocation.clearWatch(watcher);
  }, [options]);

  return { position, error } as IUsePosition;
};
