import { useLayoutEffect, useState } from 'react';
import { RUSSIA } from '../constants/2gis.constants';

interface UserCoords {
  /** Координаты юзера или дефолтные, если не удалось определить геопозицию */
  userCoords: [number, number];
  /** Флаг, обозначающий, что удалось определить геопозицию */
  isUserPosition: boolean;
}

export const useUserCoords = (fallbackCenter: [number, number] = RUSSIA): UserCoords => {
  const [userCoords, setUserCoods] = useState<[number, number]>([0, 0]);
  const [isUserPosition, setIsUserPosition] = useState(false);

  useLayoutEffect(() => {
    navigator.geolocation.watchPosition(
      position => {
        setUserCoods([position.coords.longitude, position.coords.latitude]);
        setIsUserPosition(true);
      },
      () => {
        setUserCoods(fallbackCenter);
        setIsUserPosition(false);
      }
    );
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return { userCoords, isUserPosition };
};
