/* eslint-disable @typescript-eslint/no-explicit-any */
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { BonusesSvg } from '../../assets/images/svg';
import styles from '../../assets/styles/Bonuses.module.scss';
import { BonusesCards } from '../../Components/Account/BonusesCards';
import { CARDS_CONTENT } from '../../Constants/Account.constants';

export const FirstBloc: FC = observer(() => (
  <div className={styles.firstBloc}>
    <div className={styles.header}>
      <div className={styles.headerTitle}>{CARDS_CONTENT.header}</div>
      <div className={styles.headerDesc}>{CARDS_CONTENT.headerDesc}</div>
    </div>
    <div className={styles.bigBonus}>{BonusesSvg().HeaderBonus}</div>
    <div className={styles.grid}>
      {CARDS_CONTENT.grids.map((grid, index) => (
        <BonusesCards
          key={+index}
          desc={grid.desc}
          title={grid.title}
          BonusImage={grid.svg && (BonusesSvg() as any)[grid.svg]}
        />
      ))}
    </div>
  </div>
));
