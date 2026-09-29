import React, { FC } from 'react';
import { List } from 'antd';
import { useForm } from 'antd/lib/form/Form';

import { ModelFormField, ModelFormFieldProps } from './ModelFormField';

import styles from './modelDetail.module.scss';
import { ModelDetailWrapper } from './ModelDetailWrapper';
import { FormElem } from './FormElem';

export interface ModelDetailPageProps {
  fields: ModelFormFieldProps[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  initialValues: Dictionary<any>;
  isNew: boolean;
  handleDelete?: (e: React.MouseEvent<HTMLElement>) => Promise<boolean | undefined>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  handleSave: (values: any) => Promise<any>;
  pageHeader: string;
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

export const ModelDetailPage: FC<ModelDetailPageProps> = ({
  fields,
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
  const [form] = useForm();

  return (
    <ModelDetailWrapper
      form={form}
      initialValues={initialValues}
      isNew={isNew}
      handleSave={handleSave}
      handleDelete={handleDelete}
      hideCancelButton={hideCancelButton}
      saveButtonText={saveButtonText}
      deleteButtonText={deleteButtonText}
      deleteOkText={deleteOkText}
      deleteCancelText={deleteCancelText}
      deletePopupTitle={deletePopupTitle}
      cancelPopupTitle={cancelPopupTitle}
      cancelOkText={cancelOkText}
      cancelCancelText={cancelCancelText}
      cancelButtonText={cancelButtonText}
    >
      <div className={styles.form_col}>
        {fields.map((fieldProps: ModelFormFieldProps) => (
          <FormElem key={fieldProps.name} fieldProps={fieldProps}>
            <List.Item.Meta
              title={(
                <ModelFormField
                  {...fieldProps}
                  form={form}
                  editable={fieldProps.editable}
                />
)}
              description={fieldProps.description}
            />
          </FormElem>
        ))}
      </div>
    </ModelDetailWrapper>
  );
};
