import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown } from 'antd';
import { useTranslation } from 'i18n';
import React, { FC, useEffect, useState } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { DropDownMenu } from './DropdownMenu';

export interface Props {
  setting: Record<string, boolean> | undefined;
  settingChange: (key: Record<string, boolean> | undefined) => void;
  isDisabled: boolean;
}

const SettingsTable: FC<Props> = ({
  setting, settingChange, isDisabled,
}) => {
  const [visible, setVisible] = useState<boolean>(false);
  const [localSetting, setLocalSetting] = useState<(Props['setting'] & { default?: boolean }) | undefined>(setting);
  const { t } = useTranslation();

  useEffect(() => {
    setLocalSetting(setting);
  }, [setting]);

  const handleVisibleChange = (flag: boolean) => {
    setVisible(flag);
  };

  const defaultState = () => {
    setLocalSetting(setting);
    setVisible(false);
  };

  return (
    <Dropdown
      overlay={(
        <DropDownMenu
          setVisible={setVisible}
          defaultState={defaultState}
          localSetting={localSetting}
          setLocalSetting={setLocalSetting}
          settingChange={settingChange}
        />
      )}
      visible={visible}
      onVisibleChange={handleVisibleChange}
      trigger={['click']}
    >
      <Button disabled={isDisabled}>
        {t.global.menuSettings}
        {isDisabled ? <SpinWrapped mask /> : <DownOutlined />}
      </Button>
    </Dropdown>
  );
};

export default SettingsTable;
