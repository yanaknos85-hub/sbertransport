/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC } from 'react';
import { List } from 'antd';
import { useForm } from 'antd/lib/form/Form';

import { ModelFormField, ModelFormFieldProps } from './ModelFormField';

import styles from './modelDetail.module.scss';
import { ModelDetailWrapper } from './ModelDetailWrapper';
import { FormElem } from './FormElem';

export interface ModelDetailPageProps {
  fields: ModelFormFieldProps[];
  initialValues: Dictionary<any>;
  isNew: boolean;
  handleDelete?: (e: React.MouseEvent<HTMLElement>) => Promise<boolean | undefined>;
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
  /* TODO удалено в рамках 17844 8 релиз */
  // const { logger } = useAppStoreContext();
  // const onFinish = useCallback(
  //   (vals: FormInstance<any>) => handleSave(vals).catch(err => {
  //     if (err.response?.status === 409) {
  //       logger.toMessage('error', 'В системе существует вид груза с таким названием');
  //     }
  //   }),
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  //   [form, handleSave]
  // );

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
