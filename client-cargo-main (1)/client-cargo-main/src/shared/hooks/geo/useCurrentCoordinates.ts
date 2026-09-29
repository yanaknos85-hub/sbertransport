import { useState } from 'react';
import { LatLngTuple } from 'shared/models/geo/types';

export interface CurrentCoordinates {
  currentCoordinates: LatLngTuple;
  setCurrentCoordinates: React.Dispatch<React.SetStateAction<LatLngTuple>>;
}

// Moscow coordinates
const defaultMapCoords: LatLngTuple = [55.752693, 37.617423];

const useCurrentCoordinates = (): CurrentCoordinates => {
  const [currentCoordinates, setCurrentCoordinates] = useState<LatLngTuple>(defaultMapCoords);

  return {
    currentCoordinates,
    setCurrentCoordinates,
  };
};

export default useCurrentCoordinates;
