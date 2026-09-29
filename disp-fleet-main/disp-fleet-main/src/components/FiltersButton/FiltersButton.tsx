import React, { FC } from 'react';
import { Button } from 'antd';
import { Icon } from 'components/Icon/Icon';
import styles from './FiltersButton.module.scss';

export const FiltersButton: FC<{ onClick: () => void; text?: string }> = ({ onClick, text }) => (
  <Button
    onClick={onClick}
    type="primary"
    className={styles.button}
  >
    <Icon type="control" color="#fff" />
    {text && <span className={styles.text}>{text}</span>}
  </Button>
);
