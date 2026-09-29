// TODO переделать в класс mobx, который сам будет доставать http

import { useAppStore } from 'stores/stores.context';
import { DriverStatusData, DriverCargoTripsData, CargoTrips } from './TripsCargo.types';

const TRIPS_CARGO_SERVICE = '/trips-cargo';

enum CargoTripsEndpoints {
  Online = `${TRIPS_CARGO_SERVICE}/self/online/`,
  DriverTrips = `${TRIPS_CARGO_SERVICE}/contractor/:contractorId/driver/:driverId`,
}

export const useTripsCargoService = () => {
  const { http, process } = useAppStore();

  const getDriverTrips = ({ id, contractorId }: DriverCargoTripsData) => {
    return http
      .get<CargoTrips>(CargoTripsEndpoints.DriverTrips, { urlParams: { id, contractorId } })
      .then(process.decodeResponseData(CargoTrips));
  };

  const updateDriverStatus = (data: DriverStatusData) => {
    return http.put(CargoTripsEndpoints.Online, data);
  };

  return {
    getDriverTrips,
    updateDriverStatus,
  };
};
