import React from 'react';
import { Button } from 'antd';

import styles from './FilterButton.module.scss';
import { Icon } from 'shared/components/Icon';

interface FilterButtonProps {
  title?: string;
  filterCount?: number;
  onClick?: () => void;
}

const FilterButton: React.FC<FilterButtonProps> = ({
  title, filterCount, onClick,
}) => (
  <Button
    type="primary"
    className={styles.filterButton}
    onClick={onClick}
  >
    <Icon type="control" />
    {title}
    {!!filterCount && <span className={styles.filterCount}>{filterCount}</span>}
  </Button>
);

export default FilterButton;
