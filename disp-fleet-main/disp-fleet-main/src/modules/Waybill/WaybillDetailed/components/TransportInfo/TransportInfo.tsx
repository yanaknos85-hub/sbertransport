import React from 'react';
import type { FC } from 'react';
import { useTranslation } from 'i18n';

import special from 'assets/images/car_special.png';
import flagRussia from 'assets/images/flag_russia.png';
import { Waybill } from 'api/waybill/waybill.types';

import Card from '../../components/Card';

import styles from './TransportInfo.module.scss';

interface Props {
  brand: Waybill['transport']['brand'];
  model: Waybill['transport']['model'];
  stateNumber: Waybill['transport']['stateNumber'];
  type: Waybill['transport']['transportType'];
}

const TransportInfo: FC<Props> = ({
  brand, model, stateNumber, type,
}) => {
  const { transport: i18 } = useTranslation().t.Waybill.detailed.common;

  return (
    <Card title={i18.title}>
      <div className={styles.imgWrap}>
        <img src={special} alt="car" />
      </div>

      <div className={styles.description}>
        <div className={styles.description__mainSection}>
          <span>{`${brand} ${model}`}</span>
        </div>

        <div className={styles.description__addSection}>
          <div className={styles.numberBlock}>
            <span>{stateNumber}</span>

            <img src={flagRussia} alt="flag" />
          </div>

          <div className={styles.divider} />

          <div className={styles.typeBlock}>
            <span className={styles.typeBlock__title}>{i18.type}</span>

            <span>{type}</span>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default TransportInfo;
