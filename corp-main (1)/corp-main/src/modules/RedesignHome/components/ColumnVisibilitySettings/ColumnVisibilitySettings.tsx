/* eslint-disable @typescript-eslint/no-explicit-any */
import { DownOutlined } from '@ant-design/icons';
import { Button, Dropdown } from 'antd';
import React, { FC, useEffect, useState } from 'react';

import { IHotButtons } from 'modules/RedesignHome/types/Home.types';
import { IUiPreferences } from 'stores/Home/Home.interface';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { DropDownMenu } from './DropdownMenu';
import EditingIcon from 'shared/icons/editingIcon.svg';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import styles from './styles.module.scss';

export interface Props {
  setting?: any;
  isButtonDisabled?: boolean;
  disabled?: boolean;
  transportType?: string;
  hotButtons?: IHotButtons[];
  popularServices?: IUiPreferences;
}

export const ColumnVisibilitySettings: FC<Props> = ({
  isButtonDisabled,
  disabled,
  hotButtons,
  popularServices,
}) => {
  const [visible, setVisible] = useState(false);
  const [localSetting, setLocalSetting] = useState<IHotButtons[]>();
  const { [StoreNames.homeStore]: homeStore } = useAppStoreContext();
  // const { userId } = useProfile().data;

  useEffect(() => {
    const defoultSetting = hotButtons?.map(el => {
      return { titleKey: el.titleKey, checked: true };
    });
    homeStore.saveSettings.length ? setLocalSetting(homeStore.saveSettings) : setLocalSetting(defoultSetting);

    const filterMenuItemsControls = (setting, filters) => {
      const allowedKeys = new Set(
        filters.map(item => item.valueSetting)
      );

      return setting.map(item => {
        if (allowedKeys.has(item.titleKey)) {
          return { ...item, checked: true };
        } else {
          return { ...item, checked: false };
        }
      });
    };

    if (homeStore.saveSettings.length) {
      setLocalSetting(homeStore.saveSettings);
    } else if (popularServices?.controls[0]?.settings.length) {
      const settings = filterMenuItemsControls(defoultSetting, popularServices?.controls[0]?.settings);
      setLocalSetting(settings);
    } else {
      setLocalSetting(defoultSetting);
    }
  }, [hotButtons, popularServices]);

  const handleVisibleChange = (flag: boolean) => {
    setVisible(flag);
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
          />
      )}
        visible={visible}
        onVisibleChange={handleVisibleChange}
        trigger={['click']}
      >
        <Button>
          <img src={EditingIcon} alt="settings" />
          {isButtonDisabled ? <SpinWrapped mask /> : <DownOutlined />}
        </Button>
      </Dropdown>
    </>
  );
};
