// TODO переделать в класс mobx, который сам будет доставать http

import { useAppStore } from 'stores/stores.context';
import {
  Busyness, BusynessFilters, CheckinData, CheckinInfo, CurrentTripData, DriverStatusData, DriverTripsData, PassTrip,
  PassTrips, PatchTripData,
  TripData
} from './Trips.types';

const TRIPS_SERVICE = '/trips';

enum TripsEndpoints {
  Online = `${TRIPS_SERVICE}/self/online/`,
  DriverTrips = `${TRIPS_SERVICE}/contractor/:contractorId/driver/:driverId/`,
  Busyness = `${TRIPS_SERVICE}/self/dispatcher/driver/busyness/`,
  CurrentTrip = `${TRIPS_SERVICE}/contractor/:contractorId/driver/:driverId/trip/current/`,
  Trip = `${TRIPS_SERVICE}/contractor/:contractorId/trip/:tripId/`,
  ChekinInfo = `${TRIPS_SERVICE}/contractor/:contractorId/trip/:tripId/checkin-info/`,
  DriverTrip = `${TRIPS_SERVICE}/contractor/:contractorId/driver/:driverId/trip/:tripId/`,
  Websocket = `${TRIPS_SERVICE}/ws/trips/v2`,
}

export const useTripsService = () => {
  const { http, process } = useAppStore();

  const getDriverTrips = ({
    driverId,
    contractorId,
    ...params
  }: DriverTripsData) => {
    return http
      .get<PassTrips>(TripsEndpoints.DriverTrips, {
        urlParams: { driverId, contractorId },
        params,
      })
      .then(process.decodeResponseData(PassTrips));
  };

  const getTrip = ({
    contractorId,
    driverId,
    tripId,
  }: TripData) => {
    return http
      .get<PassTrip>(TripsEndpoints.DriverTrip, {
        urlParams: {
          contractorId, driverId, tripId,
        },
      })
      .then(process.decodeResponseData(PassTrip));
  };

  const updateDriverStatus = (data: DriverStatusData) => {
    return http.put(TripsEndpoints.Online, data);
  };

  const getBusyness = (data: BusynessFilters) => {
    return http
      .post<Busyness>(TripsEndpoints.Busyness, data)
      .then(process.decodeResponseData(Busyness));
  };

  const getCurrentTrip = ({ contractorId, driverId }: CurrentTripData) => {
    return http
      .get<PassTrip>(TripsEndpoints.CurrentTrip, {
        urlParams: {
          contractorId,
          driverId,
        },
      })
      .then(process.decodeResponseData(PassTrip));
  };

  const patchTrip = ({
    contractorId, tripId, changes,
  }: PatchTripData) => {
    return http
      .patch(TripsEndpoints.Trip, changes, { urlParams: { contractorId, tripId } });
  };

  const getChekinInfo = ({ contractorId, tripId }: CheckinData) => {
    return http
      .get<CheckinInfo>(TripsEndpoints.ChekinInfo, { urlParams: { contractorId, tripId } })
      .then(process.decodeResponseData(CheckinInfo));
  };

  const getWebsocketUrl = () => TripsEndpoints.Websocket;

  return {
    getDriverTrips,
    getTrip,
    updateDriverStatus,
    getBusyness,
    getCurrentTrip,
    patchTrip,
    getChekinInfo,
    getWebsocketUrl,
  };
};
