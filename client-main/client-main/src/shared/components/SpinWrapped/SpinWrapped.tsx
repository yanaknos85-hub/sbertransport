import { Spin } from 'antd';
import classNames from 'classnames';
import React, { FC } from 'react';

import styles from './styles.module.scss';

export const SpinWrapped: FC<{
  className?: string;
  mask?: boolean;
  text?: string;
}> = ({
  className, mask, text,
}) => (
  <div
    className={classNames(styles.suspenseWrapper, className, {
      [styles.mask]: mask,
    })}
  >
    <Spin />
    {text && text.length ? <div className={styles.text}>{text}</div> : null}
  </div>
);

export default SpinWrapped;
