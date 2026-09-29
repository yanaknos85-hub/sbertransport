import React, {
  FC, Fragment, useEffect, useState
} from 'react';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';

import { hotButtons } from '../../constants/hotButtons';
import { IUiPreferences } from 'stores/Home/Home.interface';
import { IHotButtons } from 'modules/RedesignHome/types/Home.types';

import { Container } from '../Container/Container';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useProfile } from 'api/profile';
import { RedesignHotButton } from 'shared/components/RedesignHotButton/RedesignHotButton';

import styles from './HotButtons.module.scss';

export const HotButtons: FC = observer(() => {
  const { t } = useTranslation();
  const { [StoreNames.homeStore]: homeStore } = useAppStoreContext();
  const { userId } = useProfile().data;
  const [popularServices, setPopularServices] = useState<IUiPreferences>();
  const [hotButtonItems, setHotButtonItems] = useState<IHotButtons[]>();

  useEffect(() => {
    homeStore.getUiPreferences(userId, 'home_popularServices').then(el => setPopularServices(el));
  }, []);

  const filterMenuItems = (menuItems, filters) => {
    const allowedKeys = new Set(
      filters.filter(item => item.checked).map(item => item.titleKey)
    );

    return menuItems.filter(item => allowedKeys.has(item.titleKey));
  };

  const filterMenuItemsControls = (menuItems, filters) => {
    const allowedKeys = new Set(
      filters.map(item => item.valueSetting)
    );

    return menuItems.filter(item => allowedKeys.has(item.titleKey));
  };

  useEffect(() => {
    if (homeStore.saveSettings.length) {
      const settings = homeStore.saveSettings && filterMenuItems(hotButtons, homeStore.saveSettings);
      setHotButtonItems(settings);
    } else if (popularServices?.controls[0]?.settings.length) {
      const settings = filterMenuItemsControls(hotButtons, popularServices?.controls[0].settings);
      setHotButtonItems(settings);
    } else {
      setHotButtonItems(hotButtons);
    }
  }, [homeStore.saveSettings, popularServices]);

  return (
    <Container
      isPopularService
      title={t.RedesignHomePage.hotButtons}
      hotButtons={hotButtons}
      popularServices={popularServices}
    >
      <div className={styles.cards}>
        {hotButtonItems && hotButtonItems.map(({
          titleKey, icon, link, id,
        }) => (
          <Fragment key={id}>
            <RedesignHotButton
              title={t.RedesignHotButtons[titleKey] as string}
              image={icon}
              link={link}
            />
          </Fragment>
        ))}
      </div>
    </Container>
  );
});
