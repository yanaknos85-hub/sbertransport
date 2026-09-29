import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import classNames from 'classnames';
import { Tab, TabName } from '../../constants/tabs.constants';
import iconUrl from './iconUrl';

import styles from './styles.module.scss';

interface TabCardProps {
  tabKey: Tab;
}

const TabCard: FC<TabCardProps> = ({ tabKey }) => {
  const { params } = useRouteMatch();

  return (
    <div
      className={classNames(
        styles.tabCardBlock,
        styles[tabKey],
        params?.service === tabKey && styles.tabCardBlock_active
      )}
    >
      <div className={styles.tabCardBlock__infoColumn}>
        {/* Скрываем цифры, пока они фейковые */}
        <div className={styles.tabCardBlock__name}>{TabName[tabKey]}</div>
        {/* <div className={styles.tabCardBlock__all}>{new Intl.NumberFormat('ru-RU').format(orderNumbers(tabKey))}</div> */}
      </div>
      <img
        src={iconUrl[tabKey]}
        alt={`${tabKey}-icon`}
        className={classNames(styles.tabCardBlock__image, styles[tabKey])}
      />
    </div>
  );
};

export default observer(TabCard);
