import { DeleteOutlined } from '@ant-design/icons';
import { Popconfirm } from 'antd';
import React from 'react';

import styles from '../styles.module.scss';

const RemoveWaypointField = ({
  removeWaypoint,
  waypointIndex,
}: {
  waypointIndex: number;
  removeWaypoint: (index: number) => void;
}): JSX.Element => (
  <div>
    <div className={styles.addressRemoveButtonContainer}>
      <Popconfirm
        placement="left"
        title="Удалить адрес из маршрута?"
        onConfirm={(): void => {
          removeWaypoint(waypointIndex);
        }}
        okText="Да"
        cancelText="Отмена"
        style={{ width: 300 }}
      >
        <DeleteOutlined className={`${styles.addressRemoveButton}`} size={16} />
      </Popconfirm>
    </div>
  </div>
);

export default RemoveWaypointField;
