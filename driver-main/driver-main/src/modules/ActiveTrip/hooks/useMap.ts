import { useEffect } from 'react';
import dayjs from 'dayjs';
import { useRoute } from 'api/services/Geo/Geo.query';
import { PassTrip } from 'api/services/Trips/Trips.types';
import { useCheckinInfo } from 'api/services/Trips/Trips.query';
import { TRANSPORT_SERVICE_TYPES } from 'constants/geo.constants';
import { useAppStore } from 'stores/stores.context';

/** Хук управляет картой при активной поездке. Строит маршрут, ставит точки, двигает центр и т.п. */
export const useMap = (trip: PassTrip) => {
  const { mapStore } = useAppStore();

  const { data: checkinInfo, isLoading } = useCheckinInfo({ tripId: trip.id });

  const route = useRoute({
    coordinates: [
      ...(checkinInfo?.chekins[0] ? [{
        longitude: checkinInfo.chekins[0].longitude,
        latitude: checkinInfo.chekins[0].latitude,
      }] : []),
      ...trip.waypoints.map(({ longitude, latitude }) => ({
        longitude,
        latitude,
      })),
    ],
    transportServiceType: TRANSPORT_SERVICE_TYPES.EMPLOYEE_TRANSPORTATION,
  }, {
    enabled: !isLoading,
  }).data;

  useEffect(() => {
    mapStore.setPoints(trip.waypoints.map(({ longitude, latitude }) => ({
      longitude,
      latitude,
    })));
    mapStore.setCenter(mapStore.errorWatchingGeo
      ? [trip.waypoints[0].longitude, trip.waypoints[0].latitude]
      : mapStore.center
    );
  }, [mapStore, trip]);

  useEffect(() => {
    if (route) {
      mapStore.setRoutes(route.segments);
    }
  }, [mapStore, route]);

  useEffect(() => {
    if (route) {
      mapStore.setExpectedTime(route?.time);
    }
  }, [route?.time]);

  useEffect(() => {
    if (route) {
      mapStore.setExpectedDistance(route?.distance);
    }
  }, [route?.distance]);

  useEffect(() => {
    if (route) {
      const time = checkinInfo?.chekins[0] && route?.time
        ? dayjs(checkinInfo.chekins[0].time).add(Math.round(route.time), 'ms')
        : null;
      mapStore.setExpectedArrivalTime(time);
    }
  }, [checkinInfo, route?.time]);

  return {
    expectedTime: mapStore.expectedTime,
    expectedDistance: mapStore.expectedDistance,
    expectedArrivalTime: mapStore.expectedArrivalTime,
  };
};

