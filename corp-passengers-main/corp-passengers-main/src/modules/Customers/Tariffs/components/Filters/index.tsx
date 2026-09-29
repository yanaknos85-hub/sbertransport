import React, { FC, useEffect } from 'react';
import { Modal } from 'shared/components/Modal/Modal';
import { useForm } from 'antd/lib/form/Form';
import { Form } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { ContractorsFilters } from 'stores/Contractors/Contractors.interface';
import { Button } from 'shared/components/Button/Button';
import { useTranslation } from 'i18n';
import Input from 'shared/components/Inputs/Input/Input';
import { TariffsHanbooksNames } from 'modules/NewTariffs/constants/Tariffs.constants';
import { useFilterFields } from './useFilterFields';
import { useModal } from '../../context/modal.context';
import { StyledGrid, StyledItem, StyledRow } from 'modules/Customers/styled/styled.customers';
import useFilterCount from 'shared/hooks/useFilterCount';
import FilterButton from 'components/FilterButton/FilterButton';

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

  const { t } = useTranslation();

  const [form] = useForm();

  useEffect(() => {
    form.resetFields();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, form.resetFields]);

  const filtersFields = useFilterFields();

  const { filterCount } = useFilterCount(query);

  const onSearch = (values: ContractorsFilters) => {
    setQuery(values);
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
      initialValues={query}
      onFinish={onSearch}
    >
      <StyledRow>
        <StyledItem name={TariffsHanbooksNames.humanReadableId}>
          <Input.Search
            onPressEnter={form.submit}
            onSearch={form.submit}
            placeholder="Поиск по номеру тарифа"
            style={{ width: 300 }}
          />
        </StyledItem>

        <FilterButton
          title="Фильтры"
          filterCount={filterCount}
          onClick={openFilters}
        />

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
              Поиск
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
