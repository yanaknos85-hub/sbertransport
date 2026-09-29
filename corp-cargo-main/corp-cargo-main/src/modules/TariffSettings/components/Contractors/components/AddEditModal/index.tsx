import React, { FC } from 'react';
import { Modal } from 'shared/components/Modal/Modal';
import { useContractorDispatchersFull, useSingleContractor } from 'api/contractors';
import { UUID } from 'utils/io-ts';
import {
  Checkbox, Divider, Form, Popconfirm, Spin
} from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { useTranslation } from 'i18n';
import Select from 'shared/form/Select/Select';
import { IntegrationTypes, IntegrationTypesDropdown, integrationTypeTitles } from 'constants/constants.app';
import { Option } from 'shared/components/Select';
import { Button } from 'shared/components/Button/Button';
import { useResetUserPass } from 'api/reset-pass';
import { FormItem } from 'shared/components/FormItem';
import { useFields } from './hooks/useFields';
import { useModalForm } from './hooks/useModalForm';
import { StyledGrid } from 'modules/TariffSettings/styled/styled.tariffs';
import { useModal } from '../../context/modal.context';

export const AddEditModal: FC = () => {
  const { t } = useTranslation();
  const { modalState, closeModal } = useModal();

  const { data: contractor, isLoading } = useSingleContractor(
    modalState.type === 'edit' ? (modalState.id as UUID) : undefined,
    { suspense: false }
  );

  // Вынести селект диспетчеров в отдельный компонент
  const { data: dispatchers } = useContractorDispatchersFull(
    modalState.type === 'edit' ? (modalState.id as UUID) : undefined,
    {
      suspense: false,
    }
  );

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
    isUseDisp, // чекбокс использования диспетчерской
    setIsUseDisp, // чекбокс использования диспетчерской
    integrationType, // селект типа интеграции
    setIntegrationType, // селект типа интеграции
    selectedDispatcher, // селект выбора диспетчера
    setSelectedDispatcher, // селект выбора диспетчера
    isPasswordProtected, // признак того, что пароль уже был задан, и его нельзя отобразить
  } = useModalForm(contractor);

  const {
    fields, dispatcherFields, XMLFields, APIFields,
  } = useFields(modalState.type === 'add', isPasswordProtected);

  const handleUseDisp = (e: CheckboxChangeEvent) => setIsUseDisp(e.target.checked);

  const [resetUserPass] = useResetUserPass();

  const resetDispatcherPass = () => {
    if (contractor?.mainDispatcher?.id) {
      resetUserPass({ userId: contractor?.mainDispatcher?.id });
    }
  };

  const isDispExists = contractor?.integrationType === IntegrationTypes.DISPATCHER;

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={closeModal}
      onOk={form.submit}
      width={908}
      footer={[
        isDispExists && (
          <Popconfirm
            placement="top"
            title={t.contractors.resetDispatcherPasswordConfirm}
            onConfirm={resetDispatcherPass}
            okText="Сбросить"
            cancelText={t.global.cancel}
            style={{ width: 300 }}
            key="reset"
          >
            <Button type="link">{t.contractors.resetDispatcherPassword}</Button>
          </Popconfirm>
        ),
        <Button
          type="text"
          danger
          onClick={closeModal}
          key="cancel"
        >
          {t.global.cancel}
        </Button>,
        <Button
          type="primary"
          onClick={form.submit}
          key="save"
        >
          {modalState.type === 'add' ? t.global.create : t.global.save}
        </Button>,
      ]}
    >
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={saveForm}
      >
        <Spin spinning={isLoading}>
          <StyledGrid>
            {fields.map(field => (
              <ModelFormField
                key={field.name}
                {...field}
                form={form}
              />
            ))}
          </StyledGrid>

          <Form.Item style={{ marginTop: 20 }}>
            <Checkbox checked={isUseDisp} onChange={handleUseDisp}>
              {t.contractors.useDispatcher}
            </Checkbox>
          </Form.Item>

          <Divider />

          <StyledGrid>
            {!isUseDisp && (
              <>
                <FormItem label={t.contractors.integrationType}>
                  <Select value={integrationType} onChange={setIntegrationType as any}>
                    {Object.keys(integrationTypeTitles).map(type => (
                      <Option key={type} value={type}>
                        {integrationTypeTitles[type as IntegrationTypesDropdown]}
                      </Option>
                    ))}
                  </Select>
                </FormItem>

                {(integrationType === IntegrationTypes.EMAIL_XML_API
                || integrationType === IntegrationTypes.EMAIL_XML_WOUR_API)
                && XMLFields.map(field => (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                ))}

                {integrationType === IntegrationTypes.JSON_API_1_0
                && APIFields.map(field => (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                ))}
              </>
            )}

            {isUseDisp && (
              <>
                <FormItem label={t.contractors.selectDispatcher}>
                  <Select value={selectedDispatcher} onChange={setSelectedDispatcher as any}>
                    <Option value="NEW">{t.contractors.addNew}</Option>

                    {dispatchers?.map(dispatcher => (
                      <Option key={dispatcher.id} value={dispatcher.id}>
                        {`${dispatcher.firstName} ${dispatcher.lastName} ${dispatcher.patronymic}`}
                      </Option>
                    ))}
                  </Select>
                </FormItem>

                {dispatcherFields.map(field => (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                ))}
              </>
            )}
          </StyledGrid>
        </Spin>
      </Form>
    </Modal>
  );
};
