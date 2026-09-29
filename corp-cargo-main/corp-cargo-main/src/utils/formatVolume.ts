const EMPTY_SIGN = '-';

const MIN_VALUE = 0.001;

export const CUBE_TO_CM = 1_000_000;

export const formatVolume = (volume: number | null) => {
  if (!volume) {
    return EMPTY_SIGN;
  }
  return Math.max(MIN_VALUE, volume / CUBE_TO_CM).toFixed(3);
};
