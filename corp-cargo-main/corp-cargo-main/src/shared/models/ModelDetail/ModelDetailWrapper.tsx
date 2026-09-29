/* eslint-disable @typescript-eslint/no-explicit-any */
import React, {
  FC, useCallback, useEffect, useState
} from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import {
  Button, Form, List, Popconfirm
} from 'antd';
import { FormInstance } from 'antd/lib/form/Form';
import { DeleteOutlined, RollbackOutlined, SaveOutlined } from '@ant-design/icons';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import { HISTORY_PUSH_DELAY } from '../../constants/forms.constants';

import styles from './modelDetail.module.scss';

interface IModelDetailWrapper {
  form: FormInstance<any>;
  initialValues: Dictionary<any>;
  isNew: boolean;
  handleDelete?: (e: React.MouseEvent<HTMLElement>) => Promise<boolean | undefined>;
  handleSave: (values: any) => Promise<any>;
  hideCancelButton?: boolean;
  saveButtonText?: string;
  deletePopupTitle?: string;
  deleteOkText?: string;
  deleteCancelText?: string;
  deleteButtonText?: string;
  cancelPopupTitle?: string;
  cancelOkText?: string;
  cancelCancelText?: string;
  cancelButtonText?: string;
}

export const ModelDetailWrapper: FC<IModelDetailWrapper> = ({
  children,
  form,
  initialValues,
  isNew,
  handleSave,
  handleDelete,
  hideCancelButton = false,
  saveButtonText = 'Сохранить',
  deleteButtonText = 'Удалить',
  deleteOkText = 'Удалить',
  deleteCancelText = 'Отменить',
  deletePopupTitle = 'Вы уверены, что хотите удалить эту запись?',
  cancelPopupTitle = 'Вы хотите отменить изменения?',
  cancelOkText = 'Да',
  cancelCancelText = 'Отменить',
  cancelButtonText = 'Отменить',
}) => {
  const [busy, setBusy] = useState(false);

  const history = useHistory();

  const goBack = useCallback(() => {
    setTimeout(() => history.push('./'), HISTORY_PUSH_DELAY);
  }, [history]);

  const onDelete = useCallback(
    e => {
      if (handleDelete) {
        setBusy(true);
        handleDelete(e)
          .then(shouldGoBack => shouldGoBack && goBack())
          .finally(() => setBusy(false));
      }
    },
    [handleDelete, goBack]
  );

  const onFinish = useCallback(() => {
    setBusy(true);

    return handleSave(form.getFieldsValue(true))
      .then(shouldGoBack => shouldGoBack && goBack())
      .finally(() => {
        setBusy(false);
      });
  }, [form, handleSave, goBack]);

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues]);

  const deleteButton = !isNew && handleDelete && (
    <Popconfirm
      placement="top"
      title={deletePopupTitle}
      onConfirm={onDelete}
      okText={deleteOkText}
      cancelText={deleteCancelText}
      style={{ width: 300 }}
    >
      <Button
        icon={<DeleteOutlined />}
        size="middle"
        danger
      >
        {deleteButtonText}
      </Button>
    </Popconfirm>
  );

  const cancelButton = isNew && !hideCancelButton && (
    <Popconfirm
      placement="top"
      title={cancelPopupTitle}
      onConfirm={goBack}
      okText={cancelOkText}
      cancelText={cancelCancelText}
      style={{ width: 300 }}
    >
      <Button icon={<RollbackOutlined />} size="middle">
        {cancelButtonText}
      </Button>
    </Popconfirm>
  );

  const resetEditableFields = () => {
    form.setFieldsValue(initialValues);
  };

  const cancelEditButton = !hideCancelButton && !isNew && (
    <Popconfirm
      placement="top"
      title={cancelPopupTitle}
      onConfirm={resetEditableFields}
      okText={cancelOkText}
      cancelText={cancelCancelText}
      style={{ width: 300 }}
    >
      <Button icon={<RollbackOutlined />} size="middle">
        {cancelButtonText}
      </Button>
    </Popconfirm>
  );

  return (
    <div className={styles.model_detail_page}>
      <List
        itemLayout="horizontal"
        className="model_detailed"
        split={false}
      >
        <Form
          form={form}
          initialValues={initialValues}
          onFinish={onFinish}
          layout="vertical"
          name="model-detail-form"
          size="middle"
        >
          {children}

          <div className={styles.buttonBar}>
            <Button
              icon={<SaveOutlined />}
              size="middle"
              htmlType="submit"
            >
              {saveButtonText}
            </Button>
            {deleteButton}
            {cancelButton}
            {cancelEditButton}
          </div>
        </Form>
      </List>

      {busy && <SpinWrapped mask />}
    </div>
  );
};
