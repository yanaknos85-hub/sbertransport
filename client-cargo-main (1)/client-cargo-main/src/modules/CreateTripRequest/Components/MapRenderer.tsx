import React, { FC } from 'react';
import { MOSCOW } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import styles from '../styles/create.module.scss';

interface Props {
  geo: any;
  zoomControlPosition?: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' | undefined;
  locateControlPosition?: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' | undefined;
}

export const MapRenderer: FC<Props> = observer(props => {
  const {
    geo, zoomControlPosition,
  } = props;
  const segments: Segment[] = geo.calculatedRoute?.segments;
  const waypoints: WaypointModel[] = geo.waypoints.filter(point => point.isValid);
  const coordinates = waypoints.map(point => ({ latitude: point.latitude, longitude: point.longitude }));
  const position = {
    latitude: geo.currentCoordinates[0] || MOSCOW[0],
    longitude: geo.currentCoordinates[1] || MOSCOW[1],
  };

  return (
    <MapComponent
      position={position}
      markers={waypoints}
      polylines={segments || (coordinates?.length >= 2 && [{ coordinates }])}
      className={styles.map}
      dragging={true}
      zoomControl={true}
      zoomControlPosition={zoomControlPosition}
      boundsPadding={{
        top: 40, bottom: 40, left: 40, right: 400,
      }}
    />
  );
});
