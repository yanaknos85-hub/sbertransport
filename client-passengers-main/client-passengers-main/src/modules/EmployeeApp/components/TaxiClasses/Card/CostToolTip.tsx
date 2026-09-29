import React, { ReactPortal } from 'react';

import { createJsxPortal } from 'utils/portal';

import TipSvg from '../Images/right-tip.svg';

import styles from './card.module.scss';

enum TEXTS {
  title = 'Недостаточно бонусов',
  container = 'Для данного тарифа \n у Вас не хватает бонусов.',
}

interface Top { top: number | null }
type TaxiToolTrip<T> = ({ top }: Top) => T;

const taxiToolTip: TaxiToolTrip<JSX.Element> = ({ top }: Top): JSX.Element => (
  <div style={{ top: top || +!!top }} className={`${styles.toolTip} ${top && top > 0 && styles.showToolTip}`}>
    <div className={styles.markup}>
      <div className={styles.tipContainer}>
        <div className={styles.tipWrapper}>
          <span className={styles.title}>{TEXTS.title}</span>
          <span className={styles.desc}>{TEXTS.container}</span>
        </div>
      </div>
      <div className={styles.rightIcon}>
        <img
          className={styles.tipImg}
          alt="tipSvg"
          src={TipSvg}
        />
      </div>
    </div>
  </div>
);

/* Вынужденная мера из-за верстки в Такси */
export const TaxiToolTripPortal: TaxiToolTrip<ReactPortal> = ({ top }: Top): ReactPortal => createJsxPortal(taxiToolTip({ top }));
