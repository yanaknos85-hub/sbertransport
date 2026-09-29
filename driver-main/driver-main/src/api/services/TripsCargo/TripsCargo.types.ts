import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

/** Данные для вывода себя на линию/с линии */
export interface DriverStatusData {
  state: boolean;
}

export interface DriverCargoTripsData {
  id: tt.UUID;
  contractorId: tt.UUID;
}

/** Поездка */
export const CargoTrip = t.intersection([
  t.type({}),
  t.partial({}),
]);
export type CargoTrip = t.TypeOf<typeof CargoTrip>;

/** Пагинированный список грузовых поездок */
export const CargoTrips = createPagination(CargoTrip);
export type CargoTrips = t.TypeOf<typeof CargoTrips>;
