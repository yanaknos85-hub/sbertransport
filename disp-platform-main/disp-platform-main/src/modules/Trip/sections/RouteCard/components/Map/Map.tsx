import React from 'react';
import {
  HTMLMarker2GIS, Map2GIS, Marker2GIS, Polyline2GIS
} from '@sber-sbertransport/ui-kit/src';

import { FactRoute } from 'api/track/track.types';
import withErrorBoundary from 'components/withErrorBoundary';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import { useTripInfo } from 'modules/Trip/context/TripInfo.context';
import MapControl from './MapControl';
import Legend from './Legend';

import Car from 'assets/icons/map-car.svg';
import styles from './Map.module.scss';

const legend = [
  {
    color: '#BFC5CA',
    finishColor: '#FF5743',
    title: 'Плановый',
  },
  {
    color: '#10BF6A',
    title: 'Фактический',
  },
];

interface MapProps {
  planSegments: FactRoute['segments'];
  factSegments: FactRoute['segments'];
  isFinishedTrip: boolean;
}

/** Карта */
const Map = withErrorBoundary(({
  planSegments, factSegments, isFinishedTrip,
}: MapProps) => {
  const {
    trip, checkinInfo, waypoints,
  } = useTripInfo();

  const renderPolyline2GIS = ({
    segments, color, zIndex,
  }) => segments?.map((segment, index) => (
    <Polyline2GIS
      key={index}
      coordinates={segment.points.map(point => [point.longitude, point.latitude])}
      color={color}
      width={6}
      zIndex={zIndex}
    />
  ));

  return (
    <div className={styles.mapWrapper}>
      <Map2GIS
        containerStyle={{ height: '100%' }}
        center={[trip.waypoints[0]?.longitude, trip.waypoints[0]?.latitude]}
        zoom={10}
        fullScreenControl
      >
        {checkinInfo?.driverLocation && (
          <Marker2GIS
            coordinates={[checkinInfo.driverLocation.longitude, checkinInfo.driverLocation.latitude]}
            icon={Car}
          />
        )}

        {waypoints.map((waypoint, index) => (
          <HTMLMarker2GIS
            key={waypoint.id ?? waypoint.index}
            coordinates={[waypoint.longitude, waypoint.latitude]}
            userData={waypoint}
          >
            <WaypointIcon index={index} type={waypoint.type} />
          </HTMLMarker2GIS>
        ))}

        {renderPolyline2GIS({
          segments: planSegments, zIndex: 1, color: isFinishedTrip ? '#ff5743' : '#bfc5ca',
        })}
        {renderPolyline2GIS({
          segments: factSegments, zIndex: 2, color: '#10b56a',
        })}

        <MapControl>
          <Legend legend={legend} isFinishedTrip={isFinishedTrip} />
        </MapControl>
      </Map2GIS>
    </div>
  );
});

export default Map;
