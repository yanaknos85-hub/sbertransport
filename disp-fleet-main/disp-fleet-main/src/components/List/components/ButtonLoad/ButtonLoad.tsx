import React, { FC } from 'react';
import Spin from 'antd/lib/spin';

import { Icon } from 'components/Icon/Icon';
import styles from './ButtonLoad.module.scss';

interface Props {
  type: 'export' | 'import';
  loading?: boolean;
  onClick?: () => void;
}

export const ButtonLoad: FC<Props> = ({
  type, loading, onClick,
}) => (
  <button
    type="button"
    className={styles.button}
    disabled={!onClick || loading}
    onClick={onClick}
  >
    {loading ? <Spin /> : <Icon color="#909090" type={type} />}
  </button>
);
