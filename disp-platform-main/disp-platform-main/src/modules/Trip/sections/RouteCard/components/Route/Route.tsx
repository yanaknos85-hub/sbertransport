import React from 'react';
import { Timeline } from 'antd';

import withErrorBoundary from 'components/withErrorBoundary';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';

import { useTripInfo } from 'modules/Trip/context/TripInfo.context';
import WaypointInfo from 'modules/Trip/components/WaypointInfo/WaypointInfo';

import styles from './Route.module.scss';

/** Маршрут */
const Route = withErrorBoundary(() => {
  const { trip, waypoints } = useTripInfo();

  return (
    <Timeline className={styles.timeline}>
      {waypoints.map((waypoint, index) => {
        const boardingRequests = trip.requests.filter(req => req.waypoints?.[0].id === waypoint.id || (
          req.waypoints?.[0].longitude === waypoint.longitude
          && req.waypoints?.[0].latitude === waypoint.latitude
        ));

        let passengerCount = 0;
        boardingRequests.forEach(bReq => passengerCount += bReq.passengerCount);

        const passengers = trip.requests.length
          ? boardingRequests.filter(bReq => bReq.passenger).map(bReq => bReq.passenger!)
          : [waypoint.contact];

        return (
          <Timeline.Item
            key={waypoint.id ?? waypoint.index}
            dot={<WaypointIcon index={index} type={waypoint.type} />}
          >
            <WaypointInfo
              waypoint={waypoint}
              index={index}
              length={waypoints.length}
              passengerCount={passengerCount}
              passengers={passengers}
            />
          </Timeline.Item>
        );
      })}
    </Timeline>
  );
});

export default Route;
