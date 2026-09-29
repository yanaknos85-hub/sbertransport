import { ColumnType } from 'antd/lib/table';

export const columnsPropsFactory = <T = unknown>(
  columns: ColumnType<T>[],
  // eslint-disable-next-line @typescript-eslint/ban-types
  searchProps?: (key?: string | number | (string | number)[]) => {}
): ColumnType<T>[] => columns.map((column, key) => ({
    id: key,
    ...column,
    // @ts-ignore
    ...(searchProps && searchProps(column.dataIndex)),
  }));
