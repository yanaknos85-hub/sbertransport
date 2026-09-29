/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  action, computed, observable, autorun
} from 'mobx';
import * as R from 'ramda';

import { IEditableTableStore, CustomIconRenderer } from './types';

interface ConstructorProps<T> {
  data?: T[];
  rowKeyFn?: (record: T) => string;
  disableDeleteAction?: boolean;
  disableMultiRowEdit?: boolean;
  renderCustomIcons?: CustomIconRenderer<T>;
  handleSave?: (record: T) => Promise<boolean>;
  handleDelete?: (record: T) => Promise<boolean>;
  isEditable?: (record: T) => boolean;
  isDeletable?: (record: T) => boolean;
  isNewRecordPropName?: string;
}

/**
 * This is a MobX Stores which keeps track of table data.
 * It also tracks which rows are currently in edit mode and provides method to enter edit mode and methods
 * to cancel or accept/commit any changes done in edit mode
 */
export class EditableTableStore<T> implements IEditableTableStore<T> {
  /**
   * Set this flag to true to disable built-in delete record feature
   * @optional
   */
  @observable
    disableDeleteAction = false;

  /**
   * Record property which indicates whether record is new or an existing one.
   * Defaults to "isNew".
   * @optional
   */
  @observable
    isNewRecordPropName: string;

  /**
   * Set this property to true to disable multiple rows/records edits (at a time)
   */
  @observable
    disableMultiRowEdit = false;

  /**
   * Override this function in constructor to index rows by record-specific key.
   * O/w Stores record index will be used, which can cause issues when `disableDeleteAction` is set to `false`.
   * @param record record in table row
   * @returns unique key associated with the record
   */
  rowKeyFn = (record: T) => this.getRecordIndex(record).toString();

  /**
   * This function is called when user clicks save button but before table Stores is actually updated.
   * Use it to send a request to the backend in case per-record save is required.
   * @optional
   */
  handleSave?: (record: T) => Promise<boolean>;

  /**
   * This function is called when user clicks delete button but before table Stores is actually updated.
   * Use it to send a request to the backend in case per-record delete is required.
   * @optional
   */
  handleDelete?: (record: T) => Promise<boolean>;

  /**
   * Returns record from the data source. If this record is currently in edit mode - returned values will
   * represent edited values.
   * @param rowKey row key
   */
  getRecord(rowKey: string): T | undefined {
    const record = this.recordsByKey[rowKey];
    const changes = this.changesByKey.get(rowKey);
    return this.isEditing(rowKey) ? ({ ...record, ...changes } as T) : record;
  }

  /**
   * @param arg either row key or a record itself
   * @returns `true` if related record is currently in edit mode
   */
  isEditing(arg: string | T): boolean {
    const rowKey = typeof arg === 'string' ? arg : this.rowKeyFn(arg);
    return this.editingRows.has(rowKey);
  }

  /**
   * Override this function in costructor if some records should not be editable
   * @param record table record
   */
  isEditable(_: T): boolean {
    return true;
  }

  /**
   * Override this function in costructor if delete icon should not be shown for some records
   * @param record
   */
  isDeletable(_: T): boolean {
    return true;
  }

  @computed
  get isTableInEditMode(): boolean {
    return this.editingRows.size > 0;
  }

  /**
   * @param arg either row key or a record itself
   * @returns `true` if related record is currently in edit mode and has been modified recently
   */
  isModified(arg: string | T): boolean {
    const rowKey = typeof arg === 'string' ? arg : this.rowKeyFn(arg);
    return this.changesByKey.has(rowKey);
  }

  /**
   * Returns table data. Changes to the data which were not accepted yet (record is still in edit mode)
   * are not taken into account.
   */
  @computed
  get data(): T[] {
    return this.dataSource;
  }

  set data(data: T[]) {
    this.dataSource = data;
  }

  @observable
    renderCustomIcons?: CustomIconRenderer<T> | undefined;

  @observable
  private dataSource: T[] = [];

  @observable
  private editingRows = new Map<string, boolean>();

  @observable
  private changesByKey = new Map<string, Partial<T>>();

  @observable
  private recordsByKey: Dictionary<T> = {};

  constructor({
    data,
    rowKeyFn,
    disableDeleteAction,
    disableMultiRowEdit,
    handleSave,
    handleDelete,
    renderCustomIcons,
    isEditable,
    isDeletable,
    isNewRecordPropName,
  }: ConstructorProps<T>) {
    this.data = data || [];
    this.rowKeyFn = rowKeyFn || this.rowKeyFn;
    this.disableDeleteAction = disableDeleteAction ?? false;
    this.disableMultiRowEdit = disableMultiRowEdit ?? false;

    this.handleSave = handleSave;
    this.handleDelete = handleDelete;
    this.renderCustomIcons = renderCustomIcons;
    this.isEditable = isEditable ?? this.isEditable;
    this.isDeletable = isDeletable ?? this.isDeletable;

    this.isNewRecordPropName = isNewRecordPropName ?? 'isNew';

    autorun(() => {
      this.recordsByKey = R.indexBy(this.rowKeyFn)(this.dataSource);
    });
  }

  @action.bound
  startEditing(rowKey: string) {
    if (this.editingRows.has(rowKey)) {
      return; // record is in edit mode already
    }

    if (this.disableMultiRowEdit) {
      this.editingRows.clear();
      this.changesByKey.clear();
    }

    this.editingRows.set(rowKey, true);
  }

  @action.bound
  stopEditing(rowKey: string) {
    this.editingRows.delete(rowKey);
    this.changesByKey.delete(rowKey);

    // remove record from the Stores if it is "new"
    const record = this.recordsByKey[rowKey];

    if (record && (record as any)[this.isNewRecordPropName] === true) {
      const index = this.getRecordIndex(record);
      this.dataSource.splice(index, 1);
    }
  }

  @action.bound
  async deleteRecord(rowKey: string) {
    const record = this.recordsByKey[rowKey];
    const succeeded = record && this.handleDelete && (await this.handleDelete(record));

    if (succeeded) {
      this.dataSource = this.dataSource.filter(other => other !== record);
      this.stopEditing(rowKey);
    }
  }

  @action.bound
  async saveChanges(rowKey: string) {
    const record = this.getRecord(rowKey) as T;

    const succeeded = record && this.handleSave && (await this.handleSave(record));

    if (succeeded) {
      if ((record as any)[this.isNewRecordPropName] === true) {
        (record as any)[this.isNewRecordPropName] = false;
      }

      const index = this.getRecordIndex(record);
      this.dataSource[index] = record;
      this.stopEditing(rowKey);
    }
  }

  @action.bound
  onRecordEdited(rowKey: string, changes: Partial<T>) {
    this.changesByKey.set(rowKey, { ...this.changesByKey.get(rowKey), ...changes });
  }

  getRecordIndex(arg: string | T): number {
    const recordKey = typeof arg === 'string' ? arg : this.rowKeyFn(arg);
    return this.dataSource.findIndex(record => this.rowKeyFn(record) === recordKey);
  }
}
