import React, { FC, useEffect, useState } from 'react';
import {
  Button, Checkbox, Divider, Menu
} from 'antd';
import { useTranslation } from 'i18n';

import styles from '../styles.module.scss';

interface OwnProp {
  setting: Record<string, boolean> | undefined;
  settingChange: (key: Record<string, boolean> | undefined) => void;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  defaultColumns: Record<string, boolean> | undefined;
}

export const ShowSettingMenu: FC<OwnProp> = ({
  setting, settingChange, setVisible, defaultColumns,
}) => {
  const { t } = useTranslation();

  const [localSetting, setLocalSetting] = useState<(OwnProp['setting'] & { default?: boolean }) | undefined>(setting);

  const [isDefaultState, setDefaultState] = useState(true);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const isColumnVisible = (currentColumn: any) => defaultColumns && defaultColumns[currentColumn];

  const defaultState = () => {
    setLocalSetting(setting);
    setVisible(false);
  };

  useEffect(() => {
    setLocalSetting(setting);
  }, [setting]);
  const menuSettingsCheckbox = (field: keyof typeof t.Forms.informationAttributesOfRegistries) => localSetting ? (
    <Menu.Item
      key={`setting${field}`}
      icon={(
        <Checkbox
          onChange={() => {
            const settingList = { ...localSetting };
            settingList[field] = !settingList[field];
            setLocalSetting({ ...settingList });
          }}
          checked={isDefaultState ? isColumnVisible(field) : localSetting[field]}
        />
        )}
    >
      <span>{t.Forms.informationAttributesOfRegistries[field]}</span>
    </Menu.Item>
  ) : null;

  return (
    <div className={styles.menuSetting}>
      <Menu className={styles.listSetting}>
        <Menu.Item
          key="default"
          icon={<Checkbox onChange={() => setDefaultState(!isDefaultState)} checked={isDefaultState} />}
        >
          <span>{t.Forms.informationAttributesOfRegistries.default}</span>
        </Menu.Item>
        <Menu.Divider className={styles.line} />
        {localSetting
          ? (Object.keys(localSetting) as (keyof typeof t.Forms.informationAttributesOfRegistries)[]).map(
            menuSettingsCheckbox
          )
          : null}
      </Menu>
      <Divider className={styles.line} />
      <div className={styles.menuButtons}>
        <Button
          onClick={() => {
            setVisible(false);
            settingChange(isDefaultState ? defaultColumns : localSetting);
          }}
        >
          {t.global.save}
        </Button>
        <Button onClick={() => defaultState()}>{t.global.cancel}</Button>
      </div>
    </div>
  );
};
