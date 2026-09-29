import { Spin, Table as AntdTable } from 'antd';
import React, { FC } from 'react';
import cn from 'classnames';
import { TableProps } from 'antd/lib/table';
import styles from '../Table.module.scss';

type Props<T> = { className?: string; isFetching?: boolean } & TableProps<T>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const Table: FC<Props<any>> = ({
  className, isFetching, children, ...tableProps
}) => (
  <Spin spinning={isFetching}>
    <AntdTable
      {...tableProps}
      scroll={{ x: '100%' }}
      rowClassName={(_, index) => (index % 2 === 0 ? styles.rowDark : styles.rowLight)}
      size={tableProps.size ?? 'small'}
      tableLayout={tableProps.tableLayout ?? 'auto'}
      bordered={tableProps.bordered ?? false}
      className={cn(styles.table, className)}
      pagination={{
        pageSizeOptions: ['10', '20', '50', '100'],
        ...tableProps.pagination,
      }}
    />
  </Spin>
);
