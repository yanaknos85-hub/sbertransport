import React, { useEffect } from 'react';

import { SpinWrapped } from 'shared/components';
import { useChangeApprovedTripRequest } from 'shared/hooks/trip/useChangeTripRequest';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import uuid from 'utils/uuid';

import RouteWaypoint from './RouteWaypoint';

import styles from './styles.module.scss';

const EditableDiffRoute = ({
  updatedTripRequestRoute,
  oldWaypoints,
  onChangeRequest,
}: {
  oldWaypoints: WaypointModel[];
  updatedTripRequestRoute: TripRequestRoute;
  onChangeRequest: () => void;
}): JSX.Element => {
  const {
    geoWaypoints, inProgress, actualRoute, requestReadyForUpdate, request,
  } = updatedTripRequestRoute;

  const { changeTripRequest } = useChangeApprovedTripRequest({
    request,
    onChangeRequest,
  });

  useEffect(() => {
    if (requestReadyForUpdate) {
      changeTripRequest(actualRoute);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requestReadyForUpdate]);

  const newWaypoints = actualRoute.waypoints.map(waypoint => new WaypointModel(waypoint));

  return (
    <div className={styles.routeContainer}>
      <div className={styles.routeContent}>
        {newWaypoints.map((waypoint, index) => (
          <RouteWaypoint
            waypoint={waypoint}
            index={index}
            oldWaypoints={oldWaypoints}
            newWaypoints={newWaypoints}
            geoWaypoints={geoWaypoints}
            inProgress={inProgress}
            key={`${waypoint.addressStringWithRegion}--${uuid()}`}
          />
        ))}
      </div>
      {inProgress && (
        <div className={styles.routeContainerLoader}>
          <SpinWrapped text="Обновление данных" />
        </div>
      )}
    </div>
  );
};

export default EditableDiffRoute;
