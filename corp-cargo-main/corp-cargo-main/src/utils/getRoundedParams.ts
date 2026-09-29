import { CUBE_MM_TO_METERS, DefaultValues } from 'constants/constants.app';

const MIN_VALUE = 0.001;

export const getRoundedParams = ({ ...rest }) => {
  const {
    weight, volume, distance,
  } = rest;

  let resVolume;

  if (volume === 0) {
    resVolume = 0;
  } else if (volume / CUBE_MM_TO_METERS > MIN_VALUE) {
    resVolume = Number((volume / CUBE_MM_TO_METERS).toFixed(3));
  } else if (volume / CUBE_MM_TO_METERS < MIN_VALUE) {
    resVolume = MIN_VALUE;
  }

  return {
    weight: weight ? Number(weight?.toFixed(3)) : DefaultValues.emptyValueInTable,
    volume: (resVolume as number) || DefaultValues.emptyValueInTable,
    distance: distance ? Number(distance?.toFixed(1)) : DefaultValues.emptyValueInTable,
  };
};
