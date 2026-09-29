import React, { FC, useCallback } from 'react';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';
import { Popconfirm } from 'antd';
import {
  CheckOutlined, CloseOutlined, DeleteOutlined, EditOutlined
} from '@ant-design/icons';

import { EmployeesHandbookTexts, EmployeesHandbookTextsCyrillic } from 'modules/Employees/Employees.constants';
import { ActionsCellProps } from './types';

import styles from './styles.module.scss';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const ActionsCell: FC<ActionsCellProps<any>> = observer(({ record, store }) => {
  const { t } = useTranslation();

  const rowKey = store.rowKeyFn(record);

  const saveChanges = useCallback(() => store.saveChanges(rowKey), [rowKey, store]);

  const stopEditing = useCallback(() => store.stopEditing(rowKey), [rowKey, store]);

  const startEditing = useCallback(() => store.startEditing(rowKey), [rowKey, store]);

  const deleteRecord = useCallback(() => store.deleteRecord(rowKey), [rowKey, store]);

  const isEditable = store.isEditable(record);
  const isDeletable = store.isDeletable(record);
  const isEditing = store.isEditing(rowKey);
  const isNew = !!record[store.isNewRecordPropName];
  const isModified = store.isModified(rowKey);

  const saveIcon = isEditing && (
    <CheckOutlined
      key="save-icon"
      className={styles.confirmEditingIcon}
      onClick={saveChanges}
    />
  );

  const cancelConfirmIcon = isEditing && !isNew && isModified && (
    <Popconfirm
      onConfirm={stopEditing}
      placement="top"
      title={t.EditableTable.cancelEditingQuery}
      okText={t.global.closingConfirmation}
      cancelText={t.EditableTable.cancelEditingReject}
      key="cancel"
    >
      <CloseOutlined className={styles.cancelEditing} />
    </Popconfirm>
  );

  const cancelIcon = isEditing && (isNew || !isModified) && (
    <Popconfirm
      placement="left"
      title="Отменить внесённые изменения?"
      onConfirm={stopEditing}
      okText={EmployeesHandbookTextsCyrillic[EmployeesHandbookTexts.cancel]}
      cancelText="Не отменять"
      style={{ width: 300 }}
    >
      <CloseOutlined className={styles.cancelEditing} key="cancel" />
    </Popconfirm>
  );

  // DO NOT INJECT THESE PROPS
  const { isTableInEditMode, disableMultiRowEdit } = store;
  const editIcon = isEditable && !isEditing && (!disableMultiRowEdit || !isTableInEditMode) && (
    // eslint-disable-next-line jsx-a11y/interactive-supports-focus
    <div
      key="edit"
      className={styles.startEditingIcon}
      role="button"
      onClick={startEditing}
    >
      <EditOutlined />
    </div>
  );

  const deleteIcon = !store.disableDeleteAction && isDeletable && !isEditing && (
    <Popconfirm
      onConfirm={deleteRecord}
      placement="top"
      title={t.EditableTable.deleteRecordQuery}
      okText={t.global.closingConfirmation}
      cancelText={t.EditableTable.deleteRecordReject}
      key="delete"
    >
      <DeleteOutlined className={styles.delete} />
    </Popconfirm>
  );

  return (
    <div className={styles.actionsColumn}>
      {saveIcon}
      {cancelConfirmIcon}
      {cancelIcon}
      {editIcon}
      {deleteIcon}
      {store.renderCustomIcons && store.renderCustomIcons({ record, store })}
    </div>
  );
});
