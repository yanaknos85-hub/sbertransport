import Table, { TableProps } from 'antd/lib/table';
import cn from 'classnames';
import React, { FC } from 'react';
import styles from './TableShadow.module.scss';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const TableShadow: FC<TableProps<any>> = ({ className, ...other }) => (
  <Table
    rowClassName={(_, i) => (i % 2 === 1 ? styles.rowDark : '')}
    className={cn(className, styles.table)}
    {...other}
  />
);
