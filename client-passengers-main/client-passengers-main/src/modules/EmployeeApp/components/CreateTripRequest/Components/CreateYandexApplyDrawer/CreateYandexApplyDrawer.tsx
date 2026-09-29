import React, {
  Dispatch, SetStateAction, useState
} from 'react';
import { Drawer, DrawerProps, Result } from 'antd';
import { observer } from 'mobx-react';

import { StoreNames } from 'stores';
import * as routes from 'constants/constants.routes';

import TButton from 'shared/ui/Button/Button';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { YandexInfoIcon } from './icons/YandexInfoIcon';
import { useHistory } from '@sber-sbertransport/mf-core';

import { handleRedirectWithLink } from '../../utils/utils';

import styles from './CreateYandexApplyDrawer.module.scss';
import { usePlatformDetect } from 'shared/hooks/trip/usePlatformDetect';

interface CreateYandexApplyDrawerProps {
  isOpen: boolean;
  setOpen: Dispatch<SetStateAction<boolean>>;
  onClose?: () => void;
  onSubmit: () => Promise<void>;
  placement?: DrawerProps['placement'];
}

export const CreateYandexApplyDrawer = observer(({
  isOpen, setOpen, onClose, placement = 'bottom', onSubmit,
}: CreateYandexApplyDrawerProps) => {
  const { [StoreNames.tripStore]: tripStore, [StoreNames.geoStore]: geo } = useAppStoreContext();
  const history = useHistory();
  const [step, setStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const { isMobile } = usePlatformDetect();

  const onNextStep = () => {
    setIsLoading(true);
    onSubmit().then(() => {
      setStep(2);
      setTimeout(() => {
        const redirectLink = isMobile
          ? `yandextaxi://route?from=${geo.waypoints[0].latitude},${geo.waypoints[0].longitude}&to=${geo.waypoints.at(-1)?.latitude},${geo.waypoints.at(-1)?.longitude}`
          : tripStore.yandexRedirectLink;

        if (redirectLink) {
          handleRedirectWithLink(redirectLink);
        }
        setOpen?.(false);
        setIsLoading(false);
        setStep(1);
        history.push(routes.TRANSPORT_2_0_SUCCESS);
      }, 3000);
    }).catch(() => {
      setIsLoading(false);
    });
  };

  const handleOnClose = () => {
    onClose ? onClose() : setOpen(false);
  };

  return (
    <Drawer
      open={isOpen}
      onClose={handleOnClose}
      placement={placement}
      className={styles.drawerContainer}
    >
      {
        step === 1 && (
        <>
          <p className={styles.title}>
            Переход в Яндекс GO
          </p>
          <p className={styles.description}>
            Нажав «Подтвердить заказ», вы перейдете
            в Яндекс Go, будет создана поездка
            и зафиксирована предварительная стоимость.
            Если не планируете заказывать такси —
            нажмите «Отмена»
          </p>
          <div className={styles.alertContainer}>
            <YandexInfoIcon className={styles.alertIcon} />
            <p className={styles.alertTitle}>
              После прибытия подтвердите поездку в «СберТранспорт»
            </p>
          </div>
          <TButton onClick={onNextStep} disabled={isLoading}>
            Создать заявку
          </TButton>
          <TButton
            $makeLikeLink
            onClick={handleOnClose}
            disabled={isLoading}
          >
            Отменить
          </TButton>
        </>
        )
        }
      {
            step === 2 && (
            <div className={styles.successContainer}>
              <Result className={styles.successIcon} status="success" />
              <p className={styles.successTitle}>Заявка создана</p>
              <p className={styles.successSubtitle}>
                Выполняется переход в&nbsp;
                <span className={styles.successExtraTitle}>Яндекс GO</span>
              </p>
            </div>
            )
        }
    </Drawer>
  );
});
