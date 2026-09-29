import React from 'react';
import { CheckOutlined, DeleteOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { TripRequestModel } from 'stores/Trip/models';
import uuid from 'utils/uuid';
import { MIN_WAYPOINT_LENGTH } from 'utils/waypoint/core';

import styles from './styles.module.scss';

const CheckIcon = (): JSX.Element => (
  <div className={styles.CheckIcon}>
    <CheckOutlined color="#fff" size={8} />
  </div>
);

const EditableWaypoints = ({
  waypoints,
  removeWaypoint,
  request,
}: {
  waypoints: WaypointModel[];
  removeWaypoint: (index: number) => void;
  request: TripRequestModel;
}): JSX.Element => {
  const isPersonalTransport = request.transportType === 'PERSONAL';
  const disableRemoveWaypoint = waypoints.length <= MIN_WAYPOINT_LENGTH || !isPersonalTransport;

  return (
    // eslint-disable-next-line react/jsx-no-useless-fragment
    <>
      {/* FIXME react/jsx-no-useless-fragment */}
      {waypoints.map((x, index) => (
        <div
          className={styles.address}
          key={`${x.addressString}--${uuid()}`}
          title={x.addressString}
        >
          <div>{x.addressString}</div>
          {!x.checkinAutomatic ? (
            <div>
              {isPersonalTransport && (
                <div className={styles.addressAbsenceReason}>{`Причина: ${x.absenceReason || '-/-'}`}</div>
              )}
              <div className={styles.addressRemoveButtonContainer}>
                {!disableRemoveWaypoint && (
                  <Popconfirm
                    placement="left"
                    title="Удалить адрес из маршрута?"
                    onConfirm={(): void => {
                      removeWaypoint(index);
                    }}
                    okText="Да"
                    cancelText="Отмена"
                    style={{ width: 300 }}
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
          ) : (
            <CheckIcon />
          )}
        </div>
      ))}
    </>
  );
};

export default EditableWaypoints;
