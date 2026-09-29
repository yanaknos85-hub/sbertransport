import {
  Button, Checkbox
} from 'antd';
import React, {
  Dispatch, FC, SetStateAction,
  useEffect,
  useState
} from 'react';
import { useTranslation } from 'i18n';
import { StoreNames } from 'stores';

import { Icon } from 'shared/components/Icon';
import { IHotButtons, popularServicesTitle } from 'modules/RedesignHome/types/Home.types';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useProfile } from 'api/profile';
import { IUiPreferences } from 'stores/Home/Home.interface';

import styles from './styles.module.scss';

interface DropdownMenuProps {
  setVisible: Dispatch<SetStateAction<boolean>>;
  localSetting?: IHotButtons[];
}

export const DropDownMenu: FC<DropdownMenuProps> = ({
  localSetting,
  setVisible,
}) => {
  const { t } = useTranslation();
  const { [StoreNames.homeStore]: homeStore } = useAppStoreContext();
  const [settings, setSettings] = useState<IHotButtons[] | undefined>([]);
  const { userId } = useProfile().data;

  useEffect(() => {
    if (localSetting) {
      setSettings(localSetting);
    }
  }, []);

  const onChecked = (value: boolean, name: string) => {
    const result = settings?.map(setting => {
      if (setting.titleKey === name) {
        return { ...setting, checked: value };
      }

      return setting;
    });

    setSettings(result);
  };

  const transformData = (data, userID) => {
    return {
      userID,
      nameForm: 'home_popularServices',
      controls: [
        {
          typeControl: 'table',
          value: 'popularServices',
          settings: data
            .filter(settings => settings?.checked)
            .map(settings => ({
              nameSetting: 'column',
              valueSetting: settings?.titleKey,
            })),
        },
      ],
    };
  };

  const handleClose = () => {
    setVisible(false);
    setSettings(localSetting);
  };

  const handleSuccess = () => {
    const saveSettings = transformData(settings, userId);
    settings && homeStore.setSaveSettings(settings);
    homeStore.setUiPreferences(userId, saveSettings as IUiPreferences);
    setVisible(false);
  };

  return (
    <div className={styles.menuSetting}>
      <div className={styles.itemHead}>
        <p className={styles.itemsHeader}>{t.RedesignHomePage.settings.headerTitle}</p>
        <div onClick={handleClose}>
          <Icon
            type="closeModal"
            className={styles.ModalIcon}
          />
        </div>
      </div>
      <>
        <p className={styles.subtitle}>{t.RedesignHomePage.settings.subtitle}</p>
        <div className={styles.list}>
          {settings && settings.map((el, index) => (
            <div
              className={styles.item}
              key={index}
            >
              <Checkbox
                onChange={e => onChecked(e.target.checked, el.titleKey)}
                checked={el.checked}
              >
                <span>{popularServicesTitle[el.titleKey]}</span>
              </Checkbox>
            </div>
          ))}
        </div>
      </>
      <div className={styles.buttons}>
        <Button
          type="default"
          onClick={handleClose}
        >
          {t.RedesignHomePage.settings.buttons.default}
        </Button>
        <Button
          type="primary"
          // loading={isLoading}
          onClick={handleSuccess}
        >
          {t.RedesignHomePage.settings.buttons.apply}
        </Button>
      </div>
    </div>
  );
};
