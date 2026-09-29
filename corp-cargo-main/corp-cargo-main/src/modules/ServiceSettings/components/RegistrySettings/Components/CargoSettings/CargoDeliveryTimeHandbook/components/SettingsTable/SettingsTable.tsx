import React, { FC } from 'react';
import { Spin, Table } from 'antd';
import { CargoDeliveryTimeSettingsArrayType } from 'stores/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings.interface';
import { useSettingsTable } from '../../hooks/useSettingsTable';

import styles from './SettingsTable.module.scss';

export const SettingsTable: FC<{
  busy: boolean;
  cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType;
}> = ({ busy, cargoDeliveryTimeSettingsArray }) => {
  const {
    currentValues, defaultValues, columns,
  } = useSettingsTable(cargoDeliveryTimeSettingsArray);

  return (
    <Spin spinning={busy}>
      <Table
        dataSource={currentValues}
        className={styles.settingsTable}
        pagination={false}
        columns={columns}
      />
    </Spin>
  );
};
