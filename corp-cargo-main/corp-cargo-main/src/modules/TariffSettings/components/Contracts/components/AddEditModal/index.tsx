import React, { FC, useMemo, useState } from 'react';
import { Modal } from 'shared/components/Modal/Modal';
import { UUID } from 'utils/io-ts';
import { Form, Spin } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { useTranslation } from 'i18n';
import { useModalForm } from './useModalForm';
import { useModal } from '../../context/modal.context';
import { useContract } from '../../hooks/useContracts';
import { useFields } from '../../hooks/useFields';
import { StyledGrid, StyledTitle } from 'modules/TariffSettings/styled/styled.tariffs';
import { useDebounce } from 'shared/hooks/useDebounce';
import { deniedForEngineer } from '../../constants/constants';
import { Roles } from 'constants/constants.app';
import { useRole } from 'utils/useRole';

import styles from './styles.module.scss';

export const AddEditModal: FC = () => {
  const [isRequestInProgress, setRequestInProgress] = useState(false);

  const { modalState, closeModal } = useModal();

  const id = modalState.type === 'edit' ? modalState.id : undefined;
  const { data: contract, isLoading } = useContract(id as UUID, false);
  const isCreation = modalState?.type === 'add';

  const { t } = useTranslation();

  const {
    form, // форма
    saveForm, // функция для сохранения формы (создание, редактирование)
    initialValues, // дефолтные значения
    templateChecked,
    setTemplateChecked,
    purposeChecked,
    setPurposeChecked
  } = useModalForm(contract);

  const roles = useRole();
  const { fields } = useFields({
    templateChecked, setTemplateChecked, isCreation, purposeChecked, setPurposeChecked
  });

  const availableFields = useMemo(() => {
    const isMaster = roles.some(role => role === Roles.ADMIN_DATA_MASTER);
    const isEngineerCorpClient = roles.some((role) => role === Roles.ENGINEER_CORP_CLIENT);
    if (isCreation) {
      return isMaster || isEngineerCorpClient ? fields : [];
    }
    if (isMaster) {
      return fields;
    }
    if (isEngineerCorpClient) {
      return fields.filter(field => !deniedForEngineer.includes(field.name));
    }
    return [];
  }, [fields, roles, isCreation]);

  const resetRequestInProgress = useDebounce(() => {
    setRequestInProgress(false);
  }, 1500);

  const handleOk = () => {
    if (isRequestInProgress) return;
    setRequestInProgress(true);
    form.submit();
    resetRequestInProgress();
  };

  return (
    <Modal
      visible={modalState.type === 'add' || modalState.type === 'edit'}
      onCancel={() => {
        setRequestInProgress(false);
        closeModal();
      }}
      onOk={handleOk}
      width={908}
      okText={isCreation ? t.global.create : t.global.save}
    >
      <StyledTitle>
        {isCreation ? t.Contracts.addContract : t.Contracts.editContract}
      </StyledTitle>
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={saveForm}
      >
        <Spin spinning={isLoading}>
          <StyledGrid className={styles.newDesignOverrides}>
            {availableFields.map(field => (
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
