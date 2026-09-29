/* eslint-disable no-use-before-define */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { ReactNode } from 'react';
import { ColumnType, TableProps } from 'antd/lib/table';

export interface IEditableTableStore<T> {
  data: T[];

  disableDeleteAction: boolean;

  disableMultiRowEdit: boolean;

  isTableInEditMode: boolean;

  isNewRecordPropName: string;

  rowKeyFn: (record: T) => string;

  getRecord(rowKey: string): T | undefined;

  isEditing(arg: string | T): boolean;

  isModified(arg: string | T): boolean;

  isEditable: (record: T) => boolean;

  isDeletable: (record: T) => boolean;

  saveChanges(rowKey: string): Promise<void>;

  deleteRecord(rowKey: string): Promise<void>;

  startEditing(rowKey: string): void;

  stopEditing(rowKey: string): void;

  onRecordEdited(rowKey: string, changes: Partial<T>): void;

  renderCustomIcons?: CustomIconRenderer<T> | undefined;
}

export interface EditableTableProps<T> extends Omit<TableProps<T>, 'columns' | 'dataSource' | 'rowKey'> {
  columns: EditableColumnType<T>[];
  store: IEditableTableStore<T>;
  additionalColumns?: EditableColumnType<T>[];
}

export interface EditableCellProps<T, V = any> {
  cellValue: V;
  record: T;
  isEditingRecord: boolean;
  dataIndex?: string;
  index: number;
  store: IEditableTableStore<T>;
  onChange: (changes: Partial<T>) => void;
}

export interface EditableColumnType<T, V = any> extends Omit<ColumnType<T>, 'render'> {
  render?: (props: EditableCellProps<T, V>) => ReactNode;
}

export interface ActionsCellProps<T> {
  record: T;
  store: IEditableTableStore<T>;
}

export interface CustomIconRenderProps<T> {
  record: T;
  store: IEditableTableStore<T>;
}

export type CustomIconRenderer<T> = (props: CustomIconRenderProps<T>) => React.ReactNode;
