export const MAIN = '/client'; // разводная точка маршрутизации между MF
export const MOBILE_APP = `/mobile-app`;
export const AUTH = '/oauth';

export const MF_PASSENGERS = `${MAIN}/passengers`;
export const MF_CARGO = `${MAIN}/cargo`;
export const MF_FLEET = `${MAIN}/fleet`;

export const HOME = `${MAIN}/home`;
export const PAGE = `${MAIN}/page`;
export const PAGE_404 = `${MAIN}/404`;

export const APPROVEMENT = `${MAIN}/approvement`;
export const LIMITS = `${MAIN}/limits`;
export const FAVORITE = `${MAIN}/favorite`;
export const SUPPORT = `${MAIN}/support`;
export const PROFILE = `${MAIN}/profile`;
export const BONUSES = `${MAIN}/bonuses`;
export const AI = `${MAIN}/ai`;

export const VEHICLES = `${MF_PASSENGERS}/vehicles`;
export const VEHICLES_ADD = `${VEHICLES}/add`;
export const VEHICLES_DETAILED = `${VEHICLES}/:vehicleID`;

export const PASSENGER_TRIPS = `${MF_PASSENGERS}/trips/list`;

export enum PassengersMenuLinks {
  trips = 'trips',
}
