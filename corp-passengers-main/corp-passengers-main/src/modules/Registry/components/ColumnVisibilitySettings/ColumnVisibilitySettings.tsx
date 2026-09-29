/* eslint-disable @typescript-eslint/no-explicit-any */
import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown } from 'antd';
import React, { FC, useEffect, useState } from 'react';
import { ColumnsType, ColumnType } from 'antd/lib/table';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TableSettingsButton } from '../TableSettingsButton';
import { DropDownMenu } from './DropdownMenu';
import { TableRecord } from 'modules/TaxiRegistry/types/types';
import { TaxiRegistryColumnProps } from 'modules/TaxiRegistry/hooks/useColumns';
import { TripRegistryColumnProps } from 'modules/PublicRegistry/hooks/useColumns';
import { PersonalRegistryColumnProps } from 'modules/PersonalRegistry/hooks/useColumns';
import { useProfile } from 'api/profile';
import { CarSharingRegistryColumnProps } from 'modules/CarSharingRegistry/types';
import { OrderExecutionColumnProps } from 'modules/OrderExecution/components/OrderTable/Passengers/types';
import { GroupTransferReportItem } from 'stores/GroupTransferRegistry/GroupTransferRegistry';

import styles from './styles.module.scss';

export interface Props {
  setting?: any;
  saveSettingChange: (key: Record<string, boolean | undefined> | undefined) => void;
  isButtonDisabled?: boolean;
  defaultColumns?: ColumnsType<GroupTransferReportItem>
  | TaxiRegistryColumnProps[]
  | TripRegistryColumnProps[]
  | PersonalRegistryColumnProps[]
  | CarSharingRegistryColumnProps[]
  | OrderExecutionColumnProps[];
  disabled?: boolean;
  transportType?: string;
  isOto?: boolean;
}

export const ColumnVisibilitySettings: FC<Props> = ({
  setting,
  saveSettingChange,
  isButtonDisabled,
  defaultColumns,
  disabled,
  transportType,
  isOto,
}) => {
  const [visible, setVisible] = useState(false);
  const [localSetting, setLocalSetting] = useState<ColumnType<TableRecord>[] | undefined>(setting);
  const { userId } = useProfile().data;

  useEffect(() => {
    setLocalSetting(setting);
  }, [setting]);

  const handleVisibleChange = (flag: boolean) => {
    setVisible(flag);
  };

  const defaultStateOnCancel = () => {
    setLocalSetting(setting);
    setVisible(false);
  };

  return (
    <>
      <Dropdown
        disabled={disabled}
        className={styles.settingsDropdown}
        align={{ overflow: { adjustX: true, adjustY: true } }}
        overlay={(
          <DropDownMenu
            setVisible={setVisible}
            localSetting={localSetting}
            saveSettingChange={saveSettingChange}
            defaultStateOnCancel={defaultStateOnCancel}
            setLocalSetting={setLocalSetting}
            defaultColumns={defaultColumns}
            transportType={transportType}
            userId={userId}
            isOto={isOto}
          />
      )}
        visible={visible}
        onVisibleChange={handleVisibleChange}
        trigger={['click']}
      >
        <Button>
          <TableSettingsButton />
          {isButtonDisabled ? <SpinWrapped mask /> : <DownOutlined />}
        </Button>
      </Dropdown>
    </>
  );
};
