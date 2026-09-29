import { CheckOutlined } from '@ant-design/icons';
import { Form, Timeline } from 'antd';
import React from 'react';

import { plainToNew } from 'utils';

import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { WaypointField } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import RemoveWaypointField from './RemoveWaypointField';
import WaypointAddressAutoComplete from './WaypointAddressAutoComplete';
import WayPointFormField from './WayPointFormField';

import styles from '../styles.module.scss';

const CheckIcon = (): JSX.Element => (
  <div className={styles.checkIcon}>
    <CheckOutlined color="#fff" size={8} />
  </div>
);

const WaypointItem = ({
  tripRequestRoute,
  waypointField,
  isCheckInState,
  isPersonalTransport,
  waypointIndex,
}: {
  tripRequestRoute: TripRequestRoute;
  waypointField: WaypointField;
  isCheckInState: boolean;
  isPersonalTransport: boolean;
  waypointIndex: number;
}): JSX.Element => {
  const { actualRoute, geoWaypoints } = tripRequestRoute;
  const { waypoints } = actualRoute;
  const { removeWaypoint, editWaypoint } = geoWaypoints;
  const isLast = waypoints.length - 1 === waypointIndex;
  const disableRemoveWaypoint = waypointField.checkinAutomatic || waypointField.checkinManual;

  const renderAbsenceReason = (): JSX.Element => {
    const needDisplayAbsenceReason = !waypointField.checkinAutomatic && waypointField.absenceReason;
    const onEditAbsenceReason = (event: React.ChangeEvent<HTMLInputElement>): void => {
      const { value } = event.target;
      editWaypoint(waypointIndex, plainToNew(WaypointModel, { ...waypointField, absenceReason: value }));
    };
    const possibilityDisplayAbsenceReason = isCheckInState && waypointField.isValid;

    return (
      <>
        {/* Not editable case */}
        {needDisplayAbsenceReason
        && !isCheckInState
        && isPersonalTransport
        && `Причина отсутствия: ${waypointField.absenceReason}`}
        {/* Editable case */}
        {possibilityDisplayAbsenceReason && (
          <div>
            <WayPointFormField waypoint={waypointField} editAbsenceReason={onEditAbsenceReason} />
          </div>
        )}
      </>
    );
  };

  const renderRemoveField = (): JSX.Element | null => isCheckInState && !disableRemoveWaypoint ? (
    <RemoveWaypointField waypointIndex={waypointIndex} removeWaypoint={removeWaypoint} />
  ) : null;

  const renderWaypointAddressAutoComplete = (): JSX.Element | null => {
    const onEdit = (value: WaypointModel): WaypointModel[] => editWaypoint(waypointIndex, value);

    return !waypointField.isValid && isCheckInState ? (
      <Form.Item
        name={`address-${waypointField.fieldName}`}
        rules={[{ required: true, message: 'Пожалуйста, выберите адрес' }]}
      >
        <WaypointAddressAutoComplete editWaypoint={onEdit} />
      </Form.Item>
    ) : null;
  };

  return (
    <Timeline.Item
      key={waypointField.fieldName}
      className={`${styles.timelineItem} ${isLast ? styles.timelineItemLast : ''}`}
      color={waypointField.checkinAutomatic ? 'green' : 'gray'}
      dot={waypointField.checkinAutomatic ? <CheckIcon /> : null}
    >
      <div title={waypointField.addressString}>
        {`${waypointField.addressString} ${waypointField.waitingTimeString}`}
      </div>
      {renderWaypointAddressAutoComplete()}
      {!waypointField.checkinAutomatic && renderAbsenceReason()}
      {renderRemoveField()}
    </Timeline.Item>
  );
};

export default WaypointItem;
