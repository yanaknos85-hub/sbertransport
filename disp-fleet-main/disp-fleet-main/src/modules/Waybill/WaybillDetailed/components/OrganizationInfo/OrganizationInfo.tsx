import React from 'react';
import type { FC } from 'react';

import { useTranslation } from 'i18n';
import sberLogo from 'assets/icons/sber.svg';
import Card from '../../components/Card';

import styles from './OrganizationInfo.module.scss';

interface Props {
  organizationName: string;
  regionCode: string;
  msrn: string;
  tin: string;
}

const OrganizationInfo: FC<Props> = ({
  organizationName,
  regionCode,
  msrn,
  tin,
}) => {
  const { organization: i18 } = useTranslation().t.Waybill.detailed.common;

  return (
    <Card title={i18.title}>
      <div className={styles.sberIcon}>
        <img
          src={sberLogo}
          alt="Sber"
          className={styles.sberIcon__img}
        />
      </div>

      <div className={styles.description}>
        <span className={styles.description__header}>{organizationName}</span>

        <div className={styles.description__section}>
          <div className={styles.block}>
            <span className={styles.block__title}>{i18.msrn}</span>

            <span>{msrn}</span>
          </div>

          <div className={styles.divider} />

          <div className={styles.block}>
            <span className={styles.block__title}>{i18.tin}</span>

            <span>{tin}</span>
          </div>
        </div>

        <div className={styles.description__section}>
          <div className={styles.block}>
            <span className={styles.block__title}>{i18.regionCode}</span>

            <span>{regionCode}</span>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default OrganizationInfo;
