import { useEffect, useState } from 'react';

import { LatLngTuple } from 'shared/models/geo/types';

const WATCHPOSITION_INIT_OPTIONS = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 5000,
};

export interface IUsePosition {
  position: LatLngTuple;
  error: string;
}

export const useGeoPosition = (initialPosition = [0, 0], options = WATCHPOSITION_INIT_OPTIONS): IUsePosition => {
  const [position, setPosition] = useState(initialPosition);
  const [error, setError] = useState('');

  // FIXME fix any
  const onChange = ({ coords }: any): void => {
    setPosition([coords.latitude, coords.longitude]);
  };

  const onError = (er: any): void => {
    setError(er.message);
  };

  useEffect((): (() => void) => {
    const { geolocation } = navigator;

    if (!geolocation) {
      setError('Geolocation is not supported');
    }
    const watcher = geolocation.watchPosition(onChange, onError, options);

    return (): void => geolocation.clearWatch(watcher);
  }, [options]);

  return { position, error } as IUsePosition;
};
