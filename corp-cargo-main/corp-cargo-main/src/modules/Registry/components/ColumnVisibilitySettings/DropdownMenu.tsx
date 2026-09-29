import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';
import {
  Button, Checkbox, Menu
} from 'antd';
import { useTranslation } from 'i18n';
import { Props } from './ColumnVisibilitySettings';
import { ReactComponent as RepeatIcon } from 'shared/assets/svg/repeat.svg';

import styles from './styles.module.scss';

interface DropdownMenuProps {
  localSetting: (Props['setting'] & { default?: boolean }) | undefined;
  setLocalSetting: Dispatch<SetStateAction<(Props['setting'] & { default?: boolean }) | undefined>>;
  setVisible: Dispatch<SetStateAction<boolean>>;
  saveSettingChange: (key: Record<string, boolean | undefined> | undefined) => void;
  defaultStateOnCancel: () => void;
  defaultColumns?: Record<string, boolean | undefined>;
  settingsEntries?: Record<string, string>
}

export const DropDownMenu: FC<DropdownMenuProps> = ({
  localSetting,
  setLocalSetting,
  setVisible,
  saveSettingChange,
  defaultStateOnCancel,
  defaultColumns,
  settingsEntries,
}) => {
  const { t } = useTranslation();
  const [isDefaultState, setDefaultState] = useState(true);
  const isColumnVisible = (currentColumn: any) => defaultColumns && defaultColumns[currentColumn];
  const handleSuccess = () => {
    setVisible(false);
    saveSettingChange(isDefaultState ? defaultColumns : localSetting);
  };

  useEffect(() => {
    setDefaultState(is => !is);
  }, []);

  return (
    <div className={styles.menuSetting}>
      <section className={styles.menuHead}>
        <strong className={styles.menuSettingTitle}>{t.global.tableSettings}</strong>
        <button
          className={styles.resetButton}
          type="button"
          onClick={() => setDefaultState(is => !is)}
        >
          <RepeatIcon />
          {t.Forms.PublicTransportTripRequestsSettings.default}
        </button>
      </section>

      <section className={styles.itemHead}>
        <p className={styles.itemsHeader}>{t.global.order}</p>
      </section>

    <Menu className={styles.listSetting}>
      {localSetting &&
        Object.entries(localSetting).map(([key, value]) => {
          const trimmedSettingsEntries = settingsEntries && Object.entries(settingsEntries);
          const settingName = trimmedSettingsEntries?.find(
            ([label]) => label === key
          );
          if (settingName) {
            return (
              <Menu.Item
                key={key}
                onClick={() => {
                  setDefaultState(false);
                  setLocalSetting({ ...localSetting, [key]: !value });
                }}
                icon={<Checkbox checked={isDefaultState ? isColumnVisible(key) : value} />}
              >
                <span>{settingName[1]}</span>
              </Menu.Item>
            );
          }
        })}
      </Menu>

      <div className={styles.menuButtons}>
        <Button type="primary" onClick={handleSuccess}>
          {t.global.success}
        </Button>
        <Button onClick={defaultStateOnCancel}>{t.global.cancellation}</Button>
      </div>
    </div>
  );
};
