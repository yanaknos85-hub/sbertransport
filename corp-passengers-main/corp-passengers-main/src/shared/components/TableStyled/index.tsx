import { Table } from 'antd';
import { TableProps } from 'antd/lib/table';
import React, { FC } from 'react';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import cn from 'classnames';
import { Pagination } from '../Pagination';
import styles from './index.module.scss';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface ITableStyled extends TableProps<any> {
  paginationParams?: PaginationParams;
  total?: number;
  setPagination?: (pagination: PaginationParams) => void;
}

export const TableStyled: FC<ITableStyled> = ({
  paginationParams, total, setPagination, className, ...props
}) => {
  const isPagination = !!paginationParams && !!setPagination && total !== undefined;

  return (
    <>
      <Table
        pagination={false}
        className={cn(styles.table, className)}
        size="small"
        {...props}
      />

      {isPagination && (
      <Pagination
        pagination={paginationParams!}
        total={total!}
        setPagination={setPagination!}
      />
      )}
    </>
  );
};
