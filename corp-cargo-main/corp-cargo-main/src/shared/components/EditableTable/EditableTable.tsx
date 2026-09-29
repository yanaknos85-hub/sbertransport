/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC } from 'react';
import { observer } from 'mobx-react';
import Table, { ColumnType } from 'antd/lib/table';

import { EditableTableProps } from './types';
import { EditableCellWrapper } from './EditableCellWrapper';
import { ActionsCell } from './ActionsCell';

/**
 * This component is basically a wrapper for the ant's Table component.
 * It simplifies record/row edit operations and adds a special fixed-to-the-right "actions" column.
 * This column allows user to start/stop record editing, commit/confirm changes and optionally to delete record from the table.
 *
 * The main differences from ant's Table in terms of props are:
 * - component accepts `Stores` instead of the `dataSource` array. Refer to `EditableTableStore` for more details.
 * - `columns` props require each column to specify a `render` property which accepts the same properties as ant's column
 * and two additional properties - (table) `Stores` and `onChange` handler. `onChange` callback should be called in order to
 * update notify the `Stores` about the changes done to related column in related record.
 *
 *
 * Sample usage:
 * ```js
 * const useColumns = (): EditableColumnType<MyRecordType> => {
 *   const { t } = useTranslation('MyComponent');
 *
 *   return useMemo(() => [
 *     {
 *       title: t('MyTable.ColumnA'),
 *       dataIndex: 'propA',
 *       render: ({ cellValue, isEditingRecord, onChange }) => {
 *         if (isEditingRecord) {
 *           return <input value={cellValue} onChange={e => onChange(e.target.value)} />;
 *         }
 *         return <span>{cellValue}</span>;
 *       }
 *     },
 *     //...other columns
 *   ], [t]);
 * }
 *
 * const MyComponent: FC<MyComponentProps> = (props) => {
 *   const Stores = useMemo(() => new EditableTableStore<MyRecordType>({
 *     rowKeyFn: (record: MyRecordType) => record.id,
 *   }), []);
 *
 *   const columns = useColumns();
 *
 *   return (
 *     <EditableTable Stores={Stores} columns={columns} />
 *   );
 * }
 * ```
 */
export const EditableTable: FC<EditableTableProps<any>> = observer(
  ({
    store, columns, additionalColumns = [], ...other
  }) => {
    const editableColumns: ColumnType<any>[] = [
      ...columns.map((column, columnIndex) => ({
        ...column,
        key: columnIndex,
        render: (_: any, record: any, rowIndex: number) => (
          <EditableCellWrapper
            record={record}
            column={column}
            index={rowIndex}
            store={store}
          />
        ),
      })),
      {
        title: '',
        key: 'actions',
        dataIndex: '__actions__',
        fixed: 'right',
        width: 40,
        render: (_: any, record: any) => <ActionsCell record={record} store={store} />,
      },
      ...additionalColumns,
    ];

    return (
      <Table
        dataSource={[...store.data]}
        rowKey={store.rowKeyFn}
        columns={editableColumns}
        {...other}
      />
    );
  }
);
