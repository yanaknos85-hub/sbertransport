import { Col, Form } from 'antd';
import { FormListFieldData } from 'antd/lib/form/FormList';
import React, { useState } from 'react';

import { ReactComponent as WaitingTimeIcon } from 'shared/components/Images/view/time/waiting_time.svg';
import { NumericInput } from 'shared/components/NumericInput';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { plainToNew } from 'utils';

export const WaypointWaitingTime = ({
  field,
  geoWaypoints,
  index,
}: {
  field: FormListFieldData;
  geoWaypoints: GeoWaypoints;
  index: number;
}): JSX.Element => {
  const [numeric, setNumeric] = useState<number | undefined>(2);

  const onChange = (val: number | undefined): void => {
    const targetWaypoint = geoWaypoints.waypoints[index];
    const newWaypointState = plainToNew<WaypointModel>(WaypointModel, { ...targetWaypoint, waitTime: val || 0 });
    geoWaypoints.editWaypoint(index, newWaypointState);
    return setNumeric(val);
  };

  return (
    <>
      <Col>
        {' '}
        <WaitingTimeIcon />
      </Col>
      <Col span={5}>
        <Form.Item name={[field.name, 'waitTime']}>
          <NumericInput
            suffix={<small>мин</small>}
            value={numeric}
            onChange={onChange}
          />
        </Form.Item>
      </Col>
    </>
  );
};
