import React, { FC } from 'react';
import { OrUndefined } from '@sber-sbertransport/mf-core';

import styles from '../../assets/styles/Bonuses.module.scss';

interface BonusesCard {
  desc: string;
  title: string;
  BonusImage: OrUndefined<() => JSX.Element>;
}

export const BonusesCards: FC<BonusesCard> = ({
  desc, title, BonusImage,
}: BonusesCard): JSX.Element => (
  <div className={styles.frame}>
    <span className={styles.gridTitle}>{title}</span>
    <span className={styles.desc}>{desc}</span>
    {BonusImage && (
      <div className={styles.gridImage}>
        <BonusImage />
      </div>
    )}
  </div>
);
