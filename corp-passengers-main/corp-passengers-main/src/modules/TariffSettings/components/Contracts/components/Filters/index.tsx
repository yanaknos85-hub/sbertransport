import React, { FC, useEffect } from 'react';
import { IconButton } from 'shared/components/IconButton';
import { Icon } from 'shared/components/Icon';
import { Modal } from 'shared/components/Modal/Modal';
import { useForm } from 'antd/lib/form/Form';
import { Form } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
// import { ContractorsFilters } from 'stores/Contractors/Contractors.interface';
import { Button } from 'shared/components/Button/Button';
import { useTranslation } from 'i18n';
import Input from 'shared/components/Inputs/Input/Input';
import { useFilterFields } from './useFilterFields';
import { useValues } from './useValues';
import { serviceTypesDefaultValuePassengers } from '../../constants/constants';
import { useModal } from '../../context/modal.context';
import { StyledGrid, StyledItem, StyledRow } from 'modules/TariffSettings/styled/styled.tariffs';
import { Contract } from 'stores/Contracts/Contracts.interface';

interface FiltersProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  query: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setQuery: (query: any) => void;
}

export const Filters: FC<FiltersProps> = ({
  query, setQuery,
}) => {
  const {
    modalState, openFilters, closeModal,
  } = useModal();
  const { initialValues } = useValues(query);

  const { t } = useTranslation();

  const [form] = useForm();

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, form.resetFields]);

  const filtersFields = useFilterFields(modalState.type as string);

  const onSearch = (values: Partial<Contract>) => {
    const requestBody = {
      ...values,
      serviceType: serviceTypesDefaultValuePassengers.value,
      regionIds: values.regionIds && values.regionIds.length > 0 ? values.regionIds : undefined,
    };

    setQuery(requestBody);
    closeModal();
  };

  const resetFields = () => {
    setQuery({});
    closeModal();
  };

  const onCancel = () => {
    form.resetFields();
    closeModal();
  };

  return (
    <Form
      form={form}
      initialValues={initialValues}
      onFinish={onSearch}
    >
      <StyledRow>
        <StyledItem name="contractNumber">
          <Input
            noBorder={false}
            onPressEnter={form.submit}
            placeholder="Поиск по номеру договора"
            style={{ width: 300 }}
          />
        </StyledItem>

        <IconButton type="primary" onClick={openFilters}>
          <Icon
            type="control"
            color="#fff"
            className="anticon"
          />
          {' '}
          Фильтры
        </IconButton>

        <Modal
          visible={modalState.type === 'filters'}
          onCancel={onCancel}
          destroyOnClose
          width={908}
          footer={[
            <Button
              type="text"
              danger
              onClick={resetFields}
            >
              {t.global.reset}
            </Button>,
            <Button type="link" onClick={onCancel}>
              {t.global.cancel}
            </Button>,
            <Button type="primary" onClick={form.submit}>
              {t.global.search}
            </Button>,
          ]}
        >
          <StyledGrid>
            {filtersFields.map(field => (
              <ModelFormField
                key={field.name}
                form={form}
                {...field}
              />
            ))}
          </StyledGrid>
        </Modal>
      </StyledRow>
    </Form>
  );
};
