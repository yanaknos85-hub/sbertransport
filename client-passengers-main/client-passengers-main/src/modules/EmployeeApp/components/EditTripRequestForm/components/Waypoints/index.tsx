
import { Form } from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';

import { CurrentCoordinates } from 'shared/hooks/geo/useCurrentCoordinates';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';

import styles from './styles.module.scss';
import { WaypointRow } from './WaypointRow';

export const Waypoints: FC<{
  setActiveWaypointIndex: Dispatch<SetStateAction<number>>;
  geoWaypoints: GeoWaypoints;
  currentCoordsState: CurrentCoordinates;
}> = ({
  setActiveWaypointIndex, geoWaypoints, currentCoordsState,
}) => (
  <Form.List name="waypoints">
    {(fields): JSX.Element => (
      <div className={styles.waypoints}>
        {fields.map((field, index) => (
          <WaypointRow
            key={field.key}
            fields={fields}
            field={field}
            geoWaypoints={geoWaypoints}
            index={index}
            setActiveWaypointIndex={setActiveWaypointIndex}
            currentCoordsState={currentCoordsState}
          />
        ))}
      </div>
    )}
  </Form.List>
);
