import React, { FC, useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import { Divider, Form, Spin } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { useDebounce } from 'shared/hooks/useDebounce';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { useContractsById } from 'api/contracts';
import { useTranslation } from 'i18n';
import { useModalForm } from './useModalForm';
import { useModal } from '../../context/modal.context';
import { useFields } from '../../hooks/useFields';
import { StyledGrid, StyledTitle } from 'modules/Customers/styled/styled.customers';
import { ContractRestrictionTypes, ContractTypes } from 'constants/constants.app';
import { formatContractText } from '../../utils/formatContractText';

import styles from './addEditModal.module.scss';

export const AddEditModal: FC = () => {
  const { contractType } = useParams<{ contractType: ContractTypes }>();

  const { modalState, closeModal } = useModal();
  const [regionIds, setRegionIds] = useState<string[]>([]);

  const id = modalState.type === 'edit' ? modalState.id : undefined;
  const { data: contract, isLoading } = useContractsById(id as UUID, !!id, false);

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
  } = useModalForm(contract);

  const isCreation = modalState?.type === 'add';
  const {
    incomeFields, outcomeFields, restrictionType, restrictedIds,
  } = useFields({
    isCreation, regionIds,
  });

  const { t } = useTranslation();

  useEffect(() => {
    if (modalState?.type === 'edit' && contractType === ContractTypes.INCOME && contract?.regionIds) {
      setRegionIds(contract.regionIds ?? []);
    }
  }, [modalState?.type, contract, contractType]);

  const onValuesChange = useDebounce((changedValues: Contract) => {
    if (modalState.type === 'add' && contractType === ContractTypes.INCOME && 'regionIds' in changedValues) {
      setRegionIds(changedValues.regionIds);
    }
  }, 500);

  const handleCloseModal = () => {
    closeModal();
    setRegionIds([]);
  };

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={handleCloseModal}
      onOk={form.submit}
      width={908}
      okText={isCreation ? t.global.create : t.global.save}
    >
      <StyledTitle>
        {isCreation
          ? formatContractText(contractType)`Новый ${{ INCOME: 'доходный', OUTCOME: 'расходный' }} договор`
          : formatContractText(contractType)`Редактирование ${{ INCOME: 'доходного', OUTCOME: 'расходного' }} договора`}
      </StyledTitle>
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={saveForm}
        onValuesChange={onValuesChange}
      >
        <Spin spinning={isLoading}>
          <StyledGrid>
            {contractType === ContractTypes.INCOME && incomeFields.map(field => (
              <ModelFormField
                key={field.name}
                {...field}
                form={form}
              />
            ))}
            {contractType === ContractTypes.OUTCOME && outcomeFields.map(field => (
              <ModelFormField
                key={field.name}
                {...field}
                form={form}
              />
            ))}
          </StyledGrid>
          <Divider />
          <div className={styles.title}>
            {formatContractText(contractType)`Привязка к ${{ INCOME: 'расходным', OUTCOME: 'доходным' }} договорам`}
          </div>
          <div className={styles.text}>
            {formatContractText(contractType)`Договоры автоматически привязываются ко всем ${{ INCOME: 'исполнителям', OUTCOME: 'клиентам' }}, вы можете исключить ${{ INCOME: 'исполнителей', OUTCOME: 'клиентов' }} по данному договору`}
          </div>
          <ModelFormField
            {...restrictionType}
            form={form}
          />
          <Form.Item dependencies={['restrictionType']} noStyle>
            {({ getFieldValue }) => getFieldValue('restrictionType') === ContractRestrictionTypes.BLACK_LIST && (
              <ModelFormField
                form={form}
                {...restrictedIds}
              />
            )}
          </Form.Item>
        </Spin>
      </Form>
    </Modal>
  );
};
