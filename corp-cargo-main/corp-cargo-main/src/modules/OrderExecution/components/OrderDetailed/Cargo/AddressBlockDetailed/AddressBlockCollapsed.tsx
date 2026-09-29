import React, { FC } from 'react';
import classNames from 'classnames';
import { Button } from 'antd';

import styles from '../styles.module.scss';

interface Props {
  count: number;
  lastIndex: number;
  onShow: () => void;
}

export const AddressBlockCollapsed: FC<Props> = ({ count, lastIndex, onShow }) => {
  const lastLetter = String.fromCharCode(97 + lastIndex).toUpperCase();
  return (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>{lastLetter}</div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressStringCollapsed}>
          {count} адреса{' '}
          <Button className={styles.showAdditionalAddressesButton} type="link" onClick={onShow}>
            Показать
          </Button>
        </div>
      </div>
    </div>
  );
};
