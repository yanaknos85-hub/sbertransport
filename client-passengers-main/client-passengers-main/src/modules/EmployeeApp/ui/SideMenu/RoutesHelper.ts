import { TRIPS } from 'constants/constants.routes';

export const isPassengersPage = (): boolean => [
  `${TRIPS}/list/planned`,
  `${TRIPS}/list/final`,
].includes(location.pathname);
