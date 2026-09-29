import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Switch } from 'antd';

import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

import { useOnTheLineStatus } from 'api/telemechanic';
import { OnTheLineResponse } from './onTheLineSwitch.types';
import {
  TelemechanicOrderStatus,
  onTheLineSubtitle,
  titles,
  telemechanicRoutes,
  otlStorageKey
} from './onTheLineSwitch.constants';

import styles from './styles.module.scss';

const saveOnTheLineData = (data: OnTheLineResponse) => {
  const sessionStorage: Storage = window.sessionStorage;
  sessionStorage.setItem(otlStorageKey, JSON.stringify(data));
};

const OnTheLineSwitch: FC = () => {
  const history = useHistory();
  const { isDesktop } = usePlatformDetect();
  const { data: onTheLineResponse } = useOnTheLineStatus();
  const isOnTheLine = onTheLineResponse && onTheLineResponse?.requestStatus === TelemechanicOrderStatus.ON_THE_LINE;
  // при включенном ЭПЛ будет падать с ошибкой 404 если для текущего пользака нет ЭПЛ
  // и скорее всего  кнопка На линии будет перенесена в микро флит
  // а пока так, для старого пути телемеха.

  const handleClick = () => {
    saveOnTheLineData(onTheLineResponse);
    if (isOnTheLine || onTheLineResponse?.ewbId) {
      history.push(telemechanicRoutes.FLEET_TELEMECHANIC);
    } else {
      history.push(telemechanicRoutes.FLEET_TELEMECHANIC_EWB_FAILED);
    }
  };

  if (isDesktop) return null;

  return (
    <div
      className={styles.container}
      onClick={handleClick}
    >
      <div className={styles.onTheLineBlock}>
        <div className={styles.switchWrapper}>
          <div className={styles.title}>{titles.onTheLine}</div>
          <Switch checked={isOnTheLine} />
        </div>
        <div
          className={
            (onTheLineResponse?.requestStatus !== TelemechanicOrderStatus.WARNING && onTheLineResponse?.requestStatus !== TelemechanicOrderStatus.DONE)
              ? styles.subtitle
              : styles.subtitleWarning
            }
        >
          {onTheLineResponse?.requestStatus ? onTheLineSubtitle[onTheLineResponse.requestStatus] : titles.inactive}
        </div>
      </div>
    </div>
  );
};

export default observer(OnTheLineSwitch);
