import React, { FC, useCallback } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import * as routes from 'constants/constants.routes';

import { OwnershipLabels } from '../../../../constants/vehicles.constants';
import { Vehicle as TVehicle } from '../../../../types/vehicles.types';

import { ReactComponent as Icon } from './images/icon.svg';

import styles from './vehicle.module.scss';

const Vehicle: FC<TVehicle> = ({
  id, brandName, model, registrationNumber, ownerInfo,
}) => {
  const { configStore } = useAppStoreContext();
  const { history } = configStore;

  const goView = useCallback(() => history.push(`${routes.VEHICLES}/${id}`), [history, id]);

  return (
    <div className={styles.container} onClick={goView}>
      <div className={styles.icon}>
        <Icon />
      </div>
      <div className={styles.content}>
        <div className={styles.row}>
          <div className={styles.col}>
            <div className={styles.brandName}>{brandName}</div>
          </div>
          <div className={styles.col}>
            <div className={styles.status} />
          </div>
        </div>
        <div className={styles.row}>
          <div className={styles.model}>{model}</div>
        </div>
        <div className={styles.row}>
          <div className={styles.col}>
            <div className={styles.label}>Рег. номер</div>
            <div className={styles.value}>{registrationNumber}</div>
          </div>
          <div className={styles.col}>
            <div className={styles.label}>Собственность</div>
            <div className={styles.value}>{OwnershipLabels[ownerInfo]}</div>
          </div>
        </div>
      </div>
      <div className={styles.menu} />
    </div>
  );
};

export default Vehicle;
