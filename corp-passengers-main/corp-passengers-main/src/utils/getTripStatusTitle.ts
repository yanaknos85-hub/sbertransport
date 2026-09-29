import { VALUE_NOT_FOUND } from 'constants/constants.app';

export const getTripStatusTitle = (status: string, mapperObject: Record<string, string>) => {
  if (!(status in mapperObject)) {
    return VALUE_NOT_FOUND;
  }

  return mapperObject[status];
};
