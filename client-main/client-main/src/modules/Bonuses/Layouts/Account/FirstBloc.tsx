import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { BonusesSvg } from '../../assets/images/svg';
import { ReactComponent as HeaderBonus } from '../../assets/images/svg/headerBonus.svg';
import styles from '../../assets/styles/Bonuses.module.scss';
import { BonusesCards } from '../../Components/Account/BonusesCards';
import { CARDS_CONTENT } from '../../Constants/Account.constants';

export const FirstBloc: FC = observer(() => (
  <div className={styles.firstBloc}>
    <div className={styles.header}>
      <div>
        <h1 className={styles.headerTitle}>{CARDS_CONTENT.header}</h1>
        <p className={styles.headerDesc}>{CARDS_CONTENT.headerDesc}</p>
      </div>
      <HeaderBonus />
    </div>
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
