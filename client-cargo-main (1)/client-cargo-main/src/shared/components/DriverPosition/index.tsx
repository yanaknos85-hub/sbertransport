import React, { useEffect, useState } from 'react';
import L, { LatLngTuple } from 'leaflet';
import * as CarIcon from 'shared/components/Map/images/marker-icons/taxi.png';

import { useGetCurrentDriverPosition } from 'api/trip-requests';
import { TripRequestModel } from 'stores/Trip/models';
import uuid from 'utils/uuid';

import { CustomMarker } from '../Map/MapLeaflet/CustomMarker';

export const DriverPosition: React.FC<{ request: TripRequestModel }> = ({ request }): JSX.Element | null => {
  const [driverPosition, setDriverPosition] = useState<LatLngTuple | undefined>();
  const { data: position, refetch } = useGetCurrentDriverPosition(request.id);

  useEffect(() => {
    const currentPositionInterval = setTimeout(() => {
      if (
        request.status === 'TAXI_DRIVER_ON_THE_WAY'
        || request.status === 'TAXI_DRIVER_ARRIVED'
        || request.status === 'TAXI_TRIP_IN_PROGRESS'
      ) {
        refetch().then(response => {
          const latitude = response?.lastKnownPosition?.geoPoint?.latitude || 0;
          const longitude = response?.lastKnownPosition?.geoPoint?.longitude || 0;

          setDriverPosition([latitude, longitude]);
        });
      }
    }, 10 * 1000);

    return () => clearInterval(currentPositionInterval);
  }, [driverPosition, position, refetch, request.status]);

  return driverPosition ? (
    <CustomMarker
      key={uuid()}
      position={driverPosition}
      icon={
        new L.DivIcon({
          html: `<img src="${CarIcon.default}" style="width: 15px !important; height: 37px !important; transform: rotate(73deg) !important;" alt=""/>`,
          className: 'dummy',
          iconSize: [24, 48],
          iconAnchor: [12, 40],
        })
      }
    />
  ) : null;
};
