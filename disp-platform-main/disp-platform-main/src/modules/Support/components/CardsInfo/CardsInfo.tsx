import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import { getItems } from './items';
import styles from './CardsInfo.module.scss';

const CardsInfo: FC<{ isSDO: boolean }> = ({ isSDO }) => {
  const { cards, titles } = useTranslation().t.Support;

  const items = getItems(cards, isSDO);

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>{titles.support}</h3>

      <div className={styles.cardList}>
        {items.map(
          ({
            isAvailable, children, icon,
          }, index) => isAvailable && (
            <div className={styles.card} key={index}>
              <div className={styles.contacts}>
                {children.map(
                  ({
                    isAvailable, title, link,
                  }) => isAvailable && (
                  <div className={styles.contactItem} key={`${index}-${title}`}>
                    <span className={styles.title}>{title}</span>
                    <span className={styles.link}>{link}</span>
                  </div>
                  )
                )}
              </div>
              <span className={styles.icon}>{icon}</span>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default CardsInfo;
