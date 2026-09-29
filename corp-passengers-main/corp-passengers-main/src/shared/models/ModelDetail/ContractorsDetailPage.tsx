/* eslint-disable @typescript-eslint/no-explicit-any */
import React, {
  Dispatch, FC, SetStateAction, useCallback, useEffect
} from 'react';
import { ModelDetailWrapper } from 'shared/models/ModelDetail/ModelDetailWrapper';
import { ModelFormField, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { useForm } from 'antd/lib/form/Form';
import {
  List, Checkbox, Select, Form, Popconfirm, Button
} from 'antd';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { useTranslation } from 'i18n';
import { IntegrationTypes, IntegrationTypesDropdown, integrationTypeTitles } from 'constants/constants.app';
import { DeleteOutlined } from '@ant-design/icons';
import { UUID } from 'utils/io-ts';
import { FormInstance } from 'antd/es/form';
import styles from './modelDetail.module.scss';
import { FormElem } from './FormElem';
import { ContractorMapped } from 'modules/Contractors/types/types';
import { useContractorDispatcher, useContractorDispatchersFull } from 'api/contractors/index';

const renderField = (fieldProps: ModelFormFieldProps, form: FormInstance) => (
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
);

interface IContractorDetailPage {
  fields: ModelFormFieldProps[];
  dispatcherFields: ModelFormFieldProps[];
  XMLIntegrationFields: ModelFormFieldProps[];
  APIIntergrationFields: ModelFormFieldProps[];
  initialValues: ContractorMapped;
  isNew: boolean;
  handleDelete?: (e: React.MouseEvent<HTMLElement>) => Promise<boolean | undefined>;
  handleSave: (values: any) => Promise<any>;
  isUseDisp: boolean;
  setIsUseDisp: Dispatch<SetStateAction<boolean>>;
  integrationType: IntegrationTypes | undefined;
  setIntegrationType: Dispatch<SetStateAction<IntegrationTypes | undefined>>;
  onDeleteDispatchers: () => Promise<void>;
  dispExists: boolean;
  onResetDispatcherPass: () => Promise<void>;
  selectedDispatcher: UUID | 'NEW' | undefined;
  setSelectedDispatcher: Dispatch<SetStateAction<UUID | 'NEW' | undefined>>;
}

export const ContractorDetailPage: FC<IContractorDetailPage> = ({
  fields,
  dispatcherFields,
  XMLIntegrationFields,
  APIIntergrationFields,
  initialValues,
  isNew,
  handleDelete,
  handleSave,
  isUseDisp,
  setIsUseDisp,
  integrationType,
  setIntegrationType,
  onDeleteDispatchers,
  dispExists,
  onResetDispatcherPass,
  selectedDispatcher,
  setSelectedDispatcher,
}) => {
  const [form] = useForm();

  const { t } = useTranslation();

  const { data: dispatchers } = useContractorDispatchersFull(
    initialValues?.id ? (initialValues?.id as UUID) : undefined
  );

  const { data: dispatcher } = useContractorDispatcher(
    initialValues?.id ? (initialValues?.id as UUID) : undefined,
    selectedDispatcher === 'NEW' ? undefined : selectedDispatcher,
    {
      suspense: false,
    }
  );

  useEffect(() => {
    form.setFieldsValue({
      dispatcherFirstName: dispatcher?.firstName ?? '',
      dispatcherLastName: dispatcher?.lastName ?? '',
      dispatcherPatronymic: dispatcher?.patronymic ?? '',
      dispatcherPhone: dispatcher?.phone ?? '',
      dispatcherEmail: dispatcher?.email ?? '',
    });
  }, [dispatcher, form]);

  const handleUseDisp = useCallback(
    (e: CheckboxChangeEvent) => {
      setIsUseDisp(e.target.checked);
    },
    [setIsUseDisp]
  );

  return (
    <ModelDetailWrapper
      form={form}
      initialValues={initialValues}
      isNew={isNew}
      handleSave={handleSave}
      handleDelete={handleDelete}
    >
      <div className={styles.form_wrapper}>
        <div className={styles.form_col}>
          {fields.map(field => renderField(field, form))}

          <FormElem>
            <Form.Item>
              <Checkbox checked={isUseDisp} onChange={handleUseDisp}>
                {t.contractors.useDispatcher}
              </Checkbox>
            </Form.Item>
          </FormElem>

          {!isUseDisp && (
            <>
              <FormElem>
                <List.Item.Meta
                  title={(
                    <Form.Item>
                      <Select value={integrationType} onChange={setIntegrationType}>
                        {Object.keys(integrationTypeTitles).map(type => (
                          <Select.Option key={type} value={type}>
                            {integrationTypeTitles[type as IntegrationTypesDropdown]}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  )}
                  description={t.contractors.integrationType}
                />
              </FormElem>

              {(integrationType === IntegrationTypes.EMAIL_XML_API
              || integrationType === IntegrationTypes.EMAIL_XML_WOUR_API)
              && XMLIntegrationFields.map(field => renderField(field, form))}

              {integrationType === IntegrationTypes.JSON_API_1_0
              && APIIntergrationFields.map(field => renderField(field, form))}
            </>
          )}
        </div>

        {isUseDisp && (
          <div className={styles.form_col}>
            <FormElem>
              <List.Item.Meta
                title={(
                  <Form.Item>
                    <Select value={selectedDispatcher} onChange={setSelectedDispatcher}>
                      <Select.Option value="NEW">{t.contractors.addNew}</Select.Option>

                      {dispatchers.map(dispatcher => (
                        <Select.Option key={dispatcher.id} value={dispatcher.id}>
                          {`${dispatcher.firstName} ${dispatcher.lastName} ${dispatcher.patronymic}`}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                )}
                description={t.contractors.selectDispatcher}
              />
            </FormElem>

            {dispatcherFields.map(field => renderField(field, form))}

            {dispExists && (
              <Popconfirm
                placement="top"
                title={t.contractors.resetDispatcherPasswordConfirm}
                onConfirm={onResetDispatcherPass}
                okText="Удалить"
                cancelText="Отменить"
                style={{ width: 300 }}
              >
                <Button size="middle">{t.contractors.resetDispatcherPassword}</Button>
              </Popconfirm>
            )}

            {dispExists && (
              <Popconfirm
                placement="top"
                title={t.contractors.deleteDispatchersConfirm}
                onConfirm={onDeleteDispatchers}
                okText="Удалить"
                cancelText="Отменить"
                style={{ width: 300 }}
              >
                <Button
                  icon={<DeleteOutlined />}
                  size="middle"
                  danger
                >
                  Удалить диспетчерскую
                </Button>
              </Popconfirm>
            )}
          </div>
        )}
      </div>
    </ModelDetailWrapper>
  );
};
