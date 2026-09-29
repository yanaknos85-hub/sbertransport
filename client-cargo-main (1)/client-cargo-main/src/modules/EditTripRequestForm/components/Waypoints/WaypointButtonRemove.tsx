import React from 'react';
import { Button, Col } from 'antd';
import { ReactComponent as MinusIcon } from 'shared/components/Images/view/minus.svg';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';

import styles from './styles.module.scss';

export const WaypointButtonRemove = ({
  geoWaypoints,
  index,
}: {
  geoWaypoints: GeoWaypoints;
  index: number;
}): JSX.Element => {
  const onClick = (): void => {
    geoWaypoints.removeWaypoint(index);
  };

  return (
    <Col span={2}>
      <Button
        type="link"
        icon={<MinusIcon />}
        size="middle"
        block={true}
        className={styles.mrgnBtm}
        onClick={onClick}
      />
    </Col>
  );
};
