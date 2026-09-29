import React, { useEffect, useMemo, useState } from 'react';
import CargoCar from 'assets/icons/map-car-cargo.svg';
import { useTripsModal } from '../../context/TripsModal';
import styles from './index.module.scss';
import { Panel } from '../Panel';
import { useActiveTrip } from '../../context/ActiveTrip';
import moment from 'moment';
import withErrorBoundary from 'components/withErrorBoundary';
import withSuspense from 'components/withSuspense';
import { useProfile } from 'api/profile/profile.api';
import {
  DEFAULT_ZOOM, Map2GIS, Marker2GIS, ZOOM_RUSSIA, useUserCoords
} from '@sber-sbertransport/ui-kit/src';
import { DATE_FORMAT } from 'constants/app.constants';
import { DriversLocationWebsocket, SearchDriverParams } from 'api/trips-cargo/trips-cargo.types';
import { useAllCargoAvaliableDrivers, useCargoDriversLocationWebsocket } from 'api/trips-cargo/trips-cargo.api';

const REQUEST_MAP_ZOOM = 15;

export const Map = withErrorBoundary(
  withSuspense(() => {
    const { contractorId } = useProfile().data;

    const { activeTrip } = useActiveTrip();
    const { openSetRequest } = useTripsModal();

    const { userCoords, isUserPosition } = useUserCoords();

    const [driverMarkers, setDriverMarkers] = useState<DriversLocationWebsocket[]>([]);

    const { lastMessage } = useCargoDriversLocationWebsocket();

    const availableDriversParams: SearchDriverParams = useMemo(() => {
      const deadline = activeTrip ? moment(activeTrip.startTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO) : undefined;

      return {
        latitude: 1, // т.к. мы хотим отобразить на карте вообще всех водителей, нам не важно, какие координаты
        longitude: 1,
        fullSearch: true,
        enableShiftFilter: !!activeTrip,
        deadline,
      };
    }, [activeTrip]);

    const { allDrivers } = useAllCargoAvaliableDrivers(contractorId, availableDriversParams);

    useEffect(() => {
      setDriverMarkers(allDrivers.filter(x => x.longitude && x.latitude));
    }, [allDrivers]);

    useEffect(() => {
      if (!lastMessage) return;

      setDriverMarkers(drivers => {
        const driverIndex = drivers.findIndex(x => x.id === lastMessage.data.id);

        if (driverIndex !== -1) {
          drivers[driverIndex] = {
            ...drivers[driverIndex],
            ...lastMessage.data,
          };
        } else {
          const isAvailableOnMap = !activeTrip || (
            lastMessage.data.currentShift && moment
              .utc(lastMessage.data.currentShift.endDate)
              .isSameOrAfter(moment(activeTrip.startTime))
          );

          if (isAvailableOnMap) {
            drivers.push(lastMessage.data);
          }
        }

        return [...drivers.filter(x => x.longitude && x.latitude && x.online !== false)]; // в общем списке может и не бть online
      });
    }, [lastMessage]);

    const mapZoom = activeTrip ? REQUEST_MAP_ZOOM : isUserPosition ? DEFAULT_ZOOM : ZOOM_RUSSIA;

    return (
      <Panel className={styles.panel}>
        <Map2GIS
          containerStyle={{ height: '100%' }}
          center={activeTrip ? [activeTrip.waypoints[0].longitude, activeTrip.waypoints[0].latitude] : userCoords}
          zoom={mapZoom}
          fullScreenControl
        >
          {driverMarkers.map(marker => (
            <Marker2GIS
              icon={CargoCar}
              rotation={marker.azimuth ?? undefined}
              key={marker.id}
              coordinates={[marker.longitude as number, marker.latitude as number]}
              userData={marker}
              // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
              onClick={() => openSetRequest({ driverId: marker.id, shiftId: marker.currentShift?.id! })}
            />
          ))}
        </Map2GIS>
      </Panel>
    );
  })
);
