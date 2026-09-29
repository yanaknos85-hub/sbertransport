import React, { FC } from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { Form, Spin } from 'antd';
import moment from 'moment';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { FormItem } from 'shared/components/FormItem';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useTranslation } from 'i18n';
import { SelectEmployee } from 'components/SelectEmployee/SelectEmployee';
import { useModalForm } from './useModalForm';
import { useModal } from '../../context/modal.context';
import { useContract } from '../../hooks/useContracts';
import { useFields } from '../../hooks/useFields';
import { StyledGrid, StyledSubtitle, StyledTitle } from 'modules/TariffSettings/styled/styled.tariffs';
import { useProfile } from 'api/profile';
import { useOrganizationProjection } from 'api/organizations/search';
import { LabeledValue } from 'utils';

export const AddEditModal: FC = () => {
  const { organizationId } = useProfile().data;

  const { modalState, closeModal } = useModal();

  const id = modalState.type === 'edit' ? modalState.id : undefined;
  const { data: contract, isLoading } = useContract(id as UUID, false);

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
    vatChecked,
    setVatChecked,
  } = useModalForm(contract);

  const regionIds = contract?.regionIds ?? form.getFieldValue('regionIds');
  const transportType = contract?.transportType;
  const period = form.getFieldValue('period');

  const isCreation = modalState?.type === 'add';
  const { fields, penaltyFields } = useFields({
    vatChecked,
    setVatChecked,
    isCreation,
    regionIds,
    transportType,
    startDate: moment(period?.startDate),
  });

  const { t } = useTranslation();

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const onValuesChange = (changedValues: any, values: any) => {
    // Если выбранной организации плательщика нет среди выбранных оргинизаций, то очищаем поле
    if ('organizationIds' in changedValues && !changedValues.organizationIds.includes(values.paymentOrganizationId)) {
      form.setFieldsValue({
        paymentOrganizationId: undefined,
      });
    }
  };

  const organizations = useOrganizationProjection({}, { suspense: false }).data;

  const organizationOptions: LabeledValue[] = organizations?.map(({ officialName, id }) => ({
    label: officialName,
    value: id,
  })) ?? [];

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={closeModal}
      onOk={form.submit}
      width={1000}
      okText={isCreation ? t.global.create : t.global.save}
    >
      <StyledTitle>
        {isCreation ? t.Contracts.addContract : t.Contracts.editContract}
      </StyledTitle>
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={saveForm}
        onValuesChange={onValuesChange}
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
            <FormItem name="responsibleEmployeeId" label={t.Contracts.responsibleEmployeeId}>
              <SelectEmployee
                extraParams={{
                  organizations: [organizationId],
                }}
                clearOnBlur
              />
            </FormItem>
            <Form.Item noStyle dependencies={['organizationIds']}>
              {({ getFieldValue }) => (
                <FormItem
                  name="paymentOrganizationId"
                  label={t.Contracts.paymentOrganization}
                  rules={[ValidationRules.general.required]}
                >
                  <Select
                    options={organizationOptions.filter(org => getFieldValue('organizationIds')?.includes(org.value))}
                    showSearch
                    placeholder={t.Contracts.paymentOrganization}
                  />
                </FormItem>
              )}
            </Form.Item>

          </StyledGrid>

          <StyledSubtitle>{t.Contracts.penalties}</StyledSubtitle>

          <StyledGrid>
            {penaltyFields.map(field => (
              <ModelFormField
                key={field.name}
                {...field}
                form={form}
              />
            ))}
          </StyledGrid>
        </Spin>
      </Form>
    </Modal>
  );
};
