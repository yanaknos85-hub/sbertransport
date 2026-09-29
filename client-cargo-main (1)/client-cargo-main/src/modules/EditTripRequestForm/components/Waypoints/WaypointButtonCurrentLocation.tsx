import React, { useEffect, useState } from 'react';
import { FullscreenExitOutlined } from '@ant-design/icons';
import { Button, Col } from 'antd';
import { CurrentCoordinates } from 'shared/hooks/geo/useCurrentCoordinates';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';
import { useGeoPosition } from 'shared/hooks/usePosition';

import { useGetWaypointsByCoordinates } from 'api/geo';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

export const WaypointButtonCurrentLocation = ({
  currentCoordsState,
  geoWaypoints,
}: {
  currentCoordsState: CurrentCoordinates;
  geoWaypoints: GeoWaypoints;
}): JSX.Element => {
  const [fetchData, setFetchData] = useState(false);
  const { position } = useGeoPosition();

  const { data: waypoints } = useGetWaypointsByCoordinates(position, {
    enabled: position && fetchData,
    ...CLEAR_QUERY_CONFIG,
  });

  const getGeolocation = (): void => {
    setFetchData(true);
    currentCoordsState.setCurrentCoordinates(position);
    if (waypoints?.length) {
      geoWaypoints.editWaypoint(0, waypoints[0]);
    }
  };

  useEffect(() => {
    if (waypoints?.length) {
      geoWaypoints.editWaypoint(0, waypoints[0]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [waypoints]);
  // FIXME react-hooks/exhaustive-deps

  // unmount
  useEffect(
    () => (): void => {
      setFetchData(false);
    },
    []
  );

  return (
    <Col flex="32px">
      <div>
        <Button
          type="primary"
          icon={<FullscreenExitOutlined />}
          size="middle"
          onClick={getGeolocation}
        />
      </div>
    </Col>
  );
};
