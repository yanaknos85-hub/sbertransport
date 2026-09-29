import React, { FC } from 'react';
import cn from 'classnames';
import { useTranslation } from 'i18n';
import { TitleChainDtoType } from '../../types';

import styles from './styles.module.scss';

interface Props {
  titles: TitleChainDtoType[];
}

export const TitleChain: FC<Props> = ({ titles }) => {
  const { t: { Etrn } } = useTranslation();

  if (!titles || titles.length === 0) {
    return (
      <div className={styles.titleChain}>
        <div className={styles.title}>{Etrn.card.titleChainTitle}</div>
        <div className={styles.waitingText}>
          {Etrn.card.titleChainWaiting}
        </div>
      </div>
    );
  }

  return (
    <div className={styles.titleChain}>
      <div className={styles.title}>{Etrn.card.titleChainTitle}</div>
      <div className={styles.chainWrapper}>
        {titles.map((item, index) => {
          const isSigned = Boolean(item.signedAt && item.signedBy);
          const bgClass = isSigned ? styles.bg_success : styles.bg_default;
          const titleLabelKey = item.title;
          const statusText = isSigned
            ? Etrn.card.statuses.SIGNED
            : (Etrn.card.statuses[titleLabelKey] || Etrn.card.statuses.NOT_STARTED);
          const nextItem = titles[index + 1];
          const nextIsSigned = nextItem && Boolean(nextItem.signedAt && nextItem.signedBy);
          const contractor = isSigned ? item.signedBy : Etrn.card.statuses.NOT_SIGNED;
          const signerColorClass = isSigned ? styles.signerNameTextDark : styles.signerNameTextLight;
          const labelColorClass = isSigned ? styles.statusLabelTextLight : styles.textDark;

          return (
            <div key={titleLabelKey} className={styles.titleCard}>
              <div className={cn(
                styles.titleLabel,
                bgClass,
                labelColorClass)}
              >
                {Etrn.card.titleLabels[titleLabelKey] || titleLabelKey}
              </div>
              <div className={styles.statusBlock}>
                <span className={cn(styles.statusLabel, isSigned ? styles.textDark : styles.textLight)}>
                  {statusText}
                </span>
                <span className={cn(styles.signerName, signerColorClass)}>
                  {contractor}
                </span>
              </div>
              {nextItem && (
                <div
                  className={cn(
                    styles.connector,
                    nextIsSigned ? styles.connector_success : styles.connector_default
                  )}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TitleChain;
