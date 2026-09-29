import React, { FC } from 'react';
import { Icon } from 'components/Icon/Icon';
import styles from './ButtonLoad.module.scss';

interface Props {
  type: 'export' | 'import';
  onClick?: () => void;
}

export const ButtonLoad: FC<Props> = ({ type, onClick }) => (
  <button
    type="button"
    className={styles.button}
    disabled={!onClick}
    onClick={onClick}
  >
    <Icon color="#909090" type={type} />
  </button>
);
