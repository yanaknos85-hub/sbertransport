import React, { FC, useState } from 'react';
import { DownOutlined } from '@ant-design/icons';

import { Button, Dropdown } from 'antd';
import { useTranslation } from 'i18n';

import { ShowSettingMenu } from './ShowSettingMenu';

interface OwnProps {
  setting: Record<string, boolean> | undefined;
  settingChange: (key: Record<string, boolean> | undefined) => void;
  defaultColumns: Record<string, boolean> | undefined;
}

const SettingsTable: FC<OwnProps> = ({
  setting, settingChange, defaultColumns,
}) => {
  const { t } = useTranslation();

  const [visible, setVisible] = useState<boolean>(false);

  const handleVisibleChange = (flag: boolean) => {
    setVisible(flag);
  };

  return (
    <Dropdown
      overlay={(
        <ShowSettingMenu
          setting={setting}
          settingChange={settingChange}
          setVisible={setVisible}
          defaultColumns={defaultColumns}
        />
      )}
      visible={visible}
      onVisibleChange={handleVisibleChange}
      trigger={['click']}
    >
      <Button>
        {t.global.menuSettings}
        <DownOutlined />
      </Button>
    </Dropdown>
  );
};

export default SettingsTable;
