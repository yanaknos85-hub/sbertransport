/* eslint-disable @typescript-eslint/no-explicit-any */
import { FC, useCallback } from 'react';
import { observer } from 'mobx-react';

import { IEditableTableStore, EditableColumnType } from './types';

export interface EditableCellWrapperProps<T> {
  record: T;
  store: IEditableTableStore<T>;
  column: EditableColumnType<T>;
  index: number;
}

export const EditableCellWrapper: FC<EditableCellWrapperProps<any>> = observer(({
  record, store, column, index,
}) => {
  const rowKey = store.rowKeyFn(record);
  const editedRecord = store.getRecord(rowKey);
  const isEditingRecord = store.isEditing(record);

  const onChange = useCallback(
    (changes: any) => {
      store.onRecordEdited(rowKey, changes);
    },
    [rowKey, store]
  );

  if (!editedRecord) {
    return null;
  }

  const cellValue = editedRecord[column.dataIndex as any];

  const valueToRender = column.render
    ? column.render({
      cellValue,
      record: editedRecord,
      isEditingRecord,
      dataIndex: column.dataIndex as string,
      index,
      store,
      onChange,
    })
    : cellValue;

  return valueToRender;
});
