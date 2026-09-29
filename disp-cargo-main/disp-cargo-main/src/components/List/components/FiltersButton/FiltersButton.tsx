import React, { FC } from 'react';
import { Button } from 'antd';
import { Icon } from 'components/Icon/Icon';
import styles from './FiltersButton.module.scss';

export const FiltersButton: FC<{ onClick: () => void }> = ({ onClick }) => (
  <Button
    onClick={onClick}
    type="primary"
    className={styles.button}
  >
    <Icon type="control" color="#fff" />
  </Button>
);
