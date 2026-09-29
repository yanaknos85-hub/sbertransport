import { Form, Input } from 'antd';
import './overwrite.scss';
import React from 'react';

import { WaypointField } from 'shared/models/geo/types';

const WayPointFormField = ({
  waypoint,
  editAbsenceReason,
}: {
  waypoint: WaypointField;
  editAbsenceReason: (event: React.ChangeEvent<HTMLInputElement>) => void;
}): JSX.Element => {
  return (
    <Form.Item
      name={waypoint.fieldName}
      rules={[{ required: !waypoint.checkinAutomatic && !waypoint.checkinManual, message: 'Введите причину отсутствия' }]}
      shouldUpdate
      initialValue={waypoint.absenceReason}
    >
      <Input
        placeholder="Укажите причину отсутствия"
        value={waypoint.absenceReason}
        // defaultValue={waypoint.absenceReason}
        className="inputWayPoint"
        onChange={editAbsenceReason}
        disabled={waypoint.checkinAutomatic || waypoint.checkinManual}
      />
    </Form.Item>
  );
};

export default WayPointFormField;
