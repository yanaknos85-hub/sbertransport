import { CheckOutlined, DeleteOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import React from 'react';

import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { twoWaypointsIsSimilar } from 'utils/waypoint';
import { MIN_WAYPOINT_LENGTH } from 'utils/waypoint/core';

import styles from './styles.module.scss';

const CheckIcon = (): JSX.Element => (
  <div className={styles.CheckIcon}>
    <CheckOutlined color="#fff" size={8} />
  </div>
);

const RouteWaypoint = ({
  waypoint,
  index,
  oldWaypoints,
  newWaypoints,
  geoWaypoints,
  inProgress,
}: {
  waypoint: WaypointModel;
  index: number;
  oldWaypoints: WaypointModel[];
  newWaypoints: WaypointModel[];
  geoWaypoints: GeoWaypoints;
  inProgress: boolean;
}): JSX.Element => {
  const waypointIsSimilar = twoWaypointsIsSimilar(oldWaypoints[index], newWaypoints[index]);

  const handleRemove = () => {
    geoWaypoints.removeWaypoint(index);
  };

  const showRemoveWaypoint = newWaypoints.length > MIN_WAYPOINT_LENGTH;

  return (
    <div className={`${styles.address} ${waypointIsSimilar ? styles.addressSimilar : ''}`}>
      <div>{waypoint.addressStringWithRegion}</div>
      {waypointIsSimilar ? (
        <CheckIcon />
      ) : (
        <div>
          <div className={styles.addressRemoveButtonContainer}>
            {showRemoveWaypoint && (
              <Popconfirm
                placement="left"
                title="Удалить адрес из маршрута?"
                onConfirm={handleRemove}
                okText="Да"
                cancelText="Отмена"
                style={{ width: 300 }}
                disabled={inProgress}
              >
                <DeleteOutlined
                  className={`${styles.addressRemoveButton}`}
                  color="red"
                  size={16}
                />
              </Popconfirm>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default RouteWaypoint;
