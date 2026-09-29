import Table, { TableProps } from 'antd/lib/table';
import cn from 'classnames';
import React, { FC } from 'react';
import styles from './TableSpacingRows.module.scss';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const TableSpacingRows: FC<TableProps<any>> = ({ className, ...other }) => (
  <Table
    rowClassName={styles.RowStyles}
    className={cn(className, styles.Table)}
    {...other}
  />
);
