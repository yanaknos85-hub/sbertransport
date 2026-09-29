import React, { FC } from 'react';
import styles from './DefaultSort.module.scss';
import { ReactComponent as Arrow } from 'assets/icons/ArrowDownDefault.svg';
import { useTripsTabQuery } from '../../context/TripsTab.queryContext';
import useFilters from '../../hooks/useFilters';

const DefaultSort: FC = () => {
  const { setSort } = useTripsTabQuery();

  const { field, direction } = useFilters().defaultValues;

  const setDefaultSort = () => setSort({
    field,
    direction,
  });

  return (
    <div className={styles.defaultSort} onClick={setDefaultSort}>
      Сортировка по умолчанию
      <Arrow />
    </div>
  );
};

export default DefaultSort;
