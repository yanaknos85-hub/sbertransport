import { ColumnType } from 'antd/lib/table';

export const columnsPropsFactory = <T = unknown>(
  columns: ColumnType<T>[],
  searchProps?: (key?: string | number | readonly (string | number)[]) => {}
): ColumnType<T>[] => columns.map((column, key) => ({
    id: key,
    ...column,
    ...(searchProps && searchProps(column.dataIndex)),
  }));
