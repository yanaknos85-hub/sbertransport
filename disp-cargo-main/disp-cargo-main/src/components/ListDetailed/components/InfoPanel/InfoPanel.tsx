import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import { StatusesType } from '../../types';
import styles from './InfoPanel.module.scss';

type Item = { type: StatusesType; label?: never; value?: never } | { type?: never; label: string; value?: string };

export interface Props {
  items: Item[];
  title?: string;
}

export const InfoPanel: FC<Props> = ({ items, title }) => {
  const { t } = useTranslation();

  return (
    <div className={styles.container}>
      <strong className={styles.title}>{title ?? t.global.info}</strong>
      <div className={styles.listValues}>
        {items.map(item => {
          if (item.type) {
            return (
              <div className={styles.item} key="status">
                <span className={styles.label}>{t.global.status}</span>
                <span className={styles.status}>{t.Requests.Status[item.type]}</span>
              </div>
            );
          }

          return (
            <div className={styles.item} key={item.label}>
              <span className={styles.label}>{item.label}</span>
              <span className={styles.value}>{item.value}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};
