import { CheckOutlined, DeleteOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import React from 'react';

import { TripRequestModel } from 'stores/Trip/models';

import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import uuid from 'utils/uuid';
import { MIN_WAYPOINT_LENGTH } from 'utils/waypoint/core';

import styles from './styles.module.scss';
import classNames from 'classnames';

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
          key={`${x.addressStringWithRegion}--${uuid()}`}
          title={x.addressStringWithRegion}
        >
          <div className={classNames(!x.active ? styles.notActiveRoute : styles.activeRoute)}>{x.addressStringWithRegion}</div>
          {!x.checkinAutomatic ? (
            <div>
              {isPersonalTransport && (
                <div className={styles.addressAbsenceReason}>{`Причина: ${x.absenceReason || '-/-'}`}</div>
              )}
              <div className={styles.addressRemoveButtonContainer}>
                {!disableRemoveWaypoint && x.active && (
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
