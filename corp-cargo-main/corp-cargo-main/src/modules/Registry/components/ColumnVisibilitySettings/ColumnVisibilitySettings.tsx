import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown } from 'antd';
import React, { FC, useEffect, useState } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PublicUIVisibilityDTO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { TableSettingsButton } from '../TableSettingsButton';
import { DropDownMenu } from './DropdownMenu';
import styles from './styles.module.scss';

export interface Props {
  setting?: PublicUIVisibilityDTO;
  saveSettingChange: (key: Record<string, boolean | undefined> | undefined) => void;
  isButtonDisabled?: boolean;
  defaultColumns?: Record<string, boolean | undefined>;
  settingsEntries?: Record<string, string>;
  disabled?: boolean;
}

export const ColumnVisibilitySettings: FC<Props> = ({
  setting,
  saveSettingChange,
  isButtonDisabled,
  defaultColumns,
  disabled,
  settingsEntries,
}) => {
  const [visible, setVisible] = useState(false);
  const [localSetting, setLocalSetting] = useState<(Props['setting'] & { default?: boolean }) | undefined>(setting);

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
          settingsEntries={settingsEntries}
        />
      )}
      visible={visible}
      onVisibleChange={handleVisibleChange}
      trigger={['click']}
    >
      <Button disabled={isButtonDisabled}>
        <TableSettingsButton />
        {isButtonDisabled ? <SpinWrapped mask /> : <DownOutlined />}
      </Button>
    </Dropdown>
  );
};
