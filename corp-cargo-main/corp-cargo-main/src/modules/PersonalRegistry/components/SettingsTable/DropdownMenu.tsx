import {
  Button, Checkbox, Divider, Menu
} from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import styles from './styles.module.scss';
import { Props } from './SettingsTable';

interface DropdownMenuProps {
  localSetting: (Props['setting'] & { default?: boolean }) | undefined;
  setLocalSetting: Dispatch<SetStateAction<(Props['setting'] & { default?: boolean }) | undefined>>;
  setVisible: Dispatch<SetStateAction<boolean>>;
  settingChange: (key: Record<string, boolean> | undefined) => void;
  defaultState: () => void;
}

export const DropDownMenu: FC<DropdownMenuProps> = ({
  localSetting,
  setLocalSetting,
  setVisible,
  settingChange,
  defaultState,
}) => {
  const { t } = useTranslation();
  return (
    <div className={styles.menuSetting}>
      <Menu className={styles.listSetting}>
        {localSetting
          ? (Object.keys(localSetting) as (keyof typeof t.Forms.registryPersonalSettings)[]).map(key => (
            <Menu.Item
              key={key}
              icon={(
                <Checkbox
                  onChange={() => {
                    const settingList = { ...localSetting };
                    settingList[key] = !settingList[key];
                    setLocalSetting({ ...settingList });
                  }}
                  checked={localSetting[key]}
                />
                )}
            >
              <span>{t.Forms.registryPersonalSettings[key]}</span>
            </Menu.Item>
          ))
          : null}
      </Menu>
      <Divider className={styles.line} />
      <div className={styles.menuButtons}>
        <Button
          onClick={() => {
            setVisible(false);
            settingChange(localSetting);
          }}
        >
          {t.global.save}
        </Button>
        <Button onClick={() => defaultState()}>{t.global.cancel}</Button>
      </div>
    </div>
  );
};
