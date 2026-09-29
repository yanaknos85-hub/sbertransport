import React, { useEffect, useMemo, useState } from 'react';
import moment from 'moment';
import {
  DEFAULT_ZOOM, HTMLMarker2GIS, Map2GIS, Marker2GIS, ZOOM_RUSSIA, useUserCoords
} from '@sber-sbertransport/ui-kit/src';

import { useProfile } from 'api/profile/profile.api';
import { DriversLocation, DriversLocationWebsocket, SearchDriverParams } from 'api/trips/trips.types';
import { useAllAvaliableDrivers, useDriversLocationWebsocket } from 'api/trips/trips.api';
import withErrorBoundary from 'components/withErrorBoundary';
import withSuspense from 'components/withSuspense';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import { DATE_FORMAT } from 'constants/app.constants';

import { useTripsModal } from '../../context/TripsModal';
import styles from './index.module.scss';
import { Panel } from '../Panel';
import { useActiveTrip } from '../../context/ActiveTrip';
import { BusynessFilters } from './Map.constants';
import { BusynessFilter } from './BusynessFilter';

import Car from 'assets/icons/map-car.svg';
import CarGreen from 'assets/icons/map-car-green.svg';

const REQUEST_MAP_ZOOM = 15;

export const Map = withErrorBoundary(
  withSuspense(() => {
    const { contractorId } = useProfile().data;

    const { activeTrip } = useActiveTrip();
    const { openSetRequest } = useTripsModal();

    const { userCoords, isUserPosition } = useUserCoords();

    const [driverMarkers, setDriverMarkers] = useState<DriversLocationWebsocket[]>([]);
    const [busynessFilter, setBusynessFilter] = useState<BusynessFilters>();

    const { lastMessage } = useDriversLocationWebsocket();

    const availableDriversParams: SearchDriverParams = useMemo(() => {
      const deadline = activeTrip ? moment(activeTrip.expectedStartTime).add(-15, 'm').utc().format(DATE_FORMAT.ISO) : undefined;

      return {
        latitude: 1, // т.к. мы хотим отобразить на карте вообще всех водителей, нам не важно, какие координаты
        longitude: 1,
        fullSearch: true,
        enableShiftFilter: !!activeTrip,
        deadline,
      };
    }, [activeTrip]);

    const { allDrivers } = useAllAvaliableDrivers(contractorId, availableDriversParams);

    useEffect(() => {
      const checkBusyness = (driver: DriversLocation) => {
        switch (busynessFilter) {
          case BusynessFilters.Free: return !driver.activeTripId;
          case BusynessFilters.Busy: return !!driver.activeTripId;
          default: return true;
        }
      };

      setDriverMarkers(allDrivers.filter(x => x.longitude && x.latitude && checkBusyness(x)));
    }, [allDrivers, busynessFilter]);

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
              .isSameOrAfter(moment(activeTrip.expectedStartTime))
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
        <div className={styles.mapContent}>
          <div className={styles.title}>Карта</div>
          <BusynessFilter value={busynessFilter} onChange={setBusynessFilter} />
        </div>

        <Map2GIS
          containerStyle={{ height: 'calc(100% - 54px)' }}
          center={activeTrip ? [activeTrip.waypoints[0].longitude, activeTrip.waypoints[0].latitude] : userCoords}
          zoom={mapZoom}
          fullScreenControl
        >
          {driverMarkers.map(marker => (
            <Marker2GIS
              icon={marker.activeTripId ? Car : CarGreen}
              rotation={marker.azimuth ?? undefined}
              key={marker.id}
              coordinates={[marker.longitude as number, marker.latitude as number]}
              userData={marker}
              // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
              onClick={() => openSetRequest({ driverId: marker.id, shiftId: marker.currentShift?.id! })}
            />
          ))}
          {activeTrip?.waypoints?.map((waypoint, index) => (
            <HTMLMarker2GIS
              key={waypoint.id ?? waypoint.index}
              coordinates={[waypoint.longitude, waypoint.latitude]}
              userData={waypoint}
            >
              <WaypointIcon index={index} type="PASSED_AUTO" />
            </HTMLMarker2GIS>
          ))}
        </Map2GIS>
      </Panel>
    );
  })
);
