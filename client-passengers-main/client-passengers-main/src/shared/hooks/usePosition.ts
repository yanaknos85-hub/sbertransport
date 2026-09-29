import { useEffect, useState } from 'react';

import { MoscowLatLng } from 'modules/EmployeeApp/EmployeeApp.constants';
import { LatLngTuple } from 'shared/models/geo/types';

export interface IUsePosition {
  position: LatLngTuple;
  error: string;
}

export const useGeoPosition = (initialPosition = MoscowLatLng): IUsePosition => {
  const [position, setPosition] = useState(initialPosition);
  const [error, setError] = useState('');

  const onChange = ({ coords }: any): void => {
    setPosition([coords.latitude, coords.longitude]);
  };

  const onError = (er: any): void => {
    setError(er.message);
  };

  useEffect(() => {
    const { geolocation } = navigator;

    if (!geolocation) {
      setError('Geolocation is not supported');
    }

    geolocation.getCurrentPosition(onChange, onError);
  }, []);

  return { position, error } as IUsePosition;
};
