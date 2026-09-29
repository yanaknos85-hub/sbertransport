import React from 'react';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { CurrentCoordinates } from 'shared/hooks/geo/useCurrentCoordinates';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';

import styles from './styles.module.scss';

export default ({
  tripRequestRoute,
  currentCoordsState,
}: {
  tripRequestRoute: TripRequestRoute;
  currentCoordsState: CurrentCoordinates;
}): JSX.Element => {
  const { actualRoute } = tripRequestRoute;
  const position = {
    latitude: currentCoordsState.currentCoordinates[0],
    longitude: currentCoordsState.currentCoordinates[1],
  };

  return (
    <div className={styles.mapWrapper}>
      <MapComponent
        position={position}
        markers={actualRoute.waypoints}
        polylines={actualRoute.segments}
        className={styles.map}
        dragging={true}
        zoomControl={true}
      />
    </div>
  );
};
