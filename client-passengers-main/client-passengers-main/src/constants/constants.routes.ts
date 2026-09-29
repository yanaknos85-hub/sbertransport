export const EXTERNAL = '/client'; // пока в монолите висит будет так, потом можно оставить пустым

export const APP = '/passengers'; // главная точка маршрутизации приложения

export const MAIN = `${EXTERNAL}${APP}`;

export const AUTH = '/oauth';

export const TRIPS = `${MAIN}/trips`;
export const TRIPS_YANDEX = `${TRIPS}/yandex`;
export const TRIPS_CREATE = `${TRIPS}/create`;
export const TRANSPORT_2_0_CREATE = `${TRIPS_CREATE}/transport2.0`;
export const TRANSPORT_2_0_SUCCESS = `${TRANSPORT_2_0_CREATE}/success`;
export const TRIPS_CREATE_SUCCESS = `${TRIPS_CREATE}/success`;
export const TRIPS_CREATE_TAXI = `${TRIPS_CREATE}/taxi`;
export const TRIPS_CREATE_YANDEX_TAXI = `${TRIPS_CREATE}/yandex-taxi`;
export const TRIPS_CREATE_PERSONAL = `${TRIPS_CREATE}/personal`;
export const TRIPS_CREATE_PUBLIC = `${TRIPS_CREATE}/public`;
export const TRIPS_CREATE_CARSHARING = `${TRIPS_CREATE}/carsharing`;
export const TRIPS_CREATE_TRANSFER = `${TRIPS_CREATE}/transfer`;
export const TRIPS_CREATE_BUS = `${TRIPS_CREATE}/bus`;
export const TRIPS_CREATE_COOPERATIVE = `${TRIPS_CREATE}/cooperative`;
export const TRIPS_LIST = `${TRIPS}/list`;
export const TRIPS_LIST_PLANNED = `${TRIPS_LIST}/planned`;
export const TRIPS_LIST_FINAL = `${TRIPS_LIST}/final`;
export const TRIPS_LIST_YANDEX_PLANNED = `${TRIPS_YANDEX}/planned`;
export const TRIPS_JOURNAL = `${TRIPS_LIST}/:filter`;
export const TRIPS_YANDEX_JOURNAL = `${TRIPS_YANDEX}/:filter`;
export const TRIPS_DETAILED = `${TRIPS_LIST}/:filter/:reqId`;
export const TRIPS_YANDEX_DETAILED = `${TRIPS_YANDEX}/:filter/:reqId`;

export const APPROVEMENT = `${MAIN}/approvement`;
export const APPROVEMENT_TRIPS = `${APPROVEMENT}/trips`;
export const APPROVEMENT_TRIPS_JOURNAL = `${APPROVEMENT_TRIPS}/:filter`;
export const APPROVEMENT_TRIPS_DETAILED = `${APPROVEMENT_TRIPS}/:filter/:reqId`;

export const APPROVEMENT_YANDEX = `${APPROVEMENT}/external`;
export const APPROVEMENT_YANDEX_JOURNAL = `${APPROVEMENT_YANDEX}/:filter`;
export const APPROVEMENT_YANDEX_DETAILED = `${APPROVEMENT_YANDEX}/:filter/:reqId`;

export const APPROVEMENT_DELEGATE = `${APPROVEMENT}/delegate`;
export const APPROVEMENT_DELEGATE_CREATE = `${APPROVEMENT_DELEGATE}/create`;
export const APPROVEMENT_DELEGATES = `${APPROVEMENT}/delegates`;

export const FAVORITE = `${MAIN}/favorite`;

export const BONUSES = `${MAIN}/bonuses`;
export const BONUSES_ACCOUNT_PAGE = `${BONUSES}/account`;

export const PERSONAL_CARS = `${MAIN}/personalCars`;
export const PERSONAL_CARS_DETAILED = `${PERSONAL_CARS}/:personalCarId`;

export const PROFILE = `${EXTERNAL}/profile`;

export const VEHICLES = `${MAIN}/vehicles`;
export const VEHICLES_ADD = `${VEHICLES}/add`;
export const VEHICLES_DETAILED = `${VEHICLES}/:vehicleID`;

export enum PassengersMenuLinks {
  trips = 'trips',
}
