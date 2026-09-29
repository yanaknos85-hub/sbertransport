import React, { FC, useEffect } from 'react';
import { IconButton } from 'shared/components/IconButton';
import { Icon } from 'shared/components/Icon';
import { Modal } from 'shared/components/Modal/Modal';
import { useForm } from 'antd/lib/form/Form';
import { Form } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { ContractorsFilters } from 'stores/Contractors/Contractors.interface';
import { Button } from 'shared/components/Button/Button';
import { useTranslation } from 'i18n';
import Input from 'shared/components/Inputs/Input/Input';
import { ContractorHandbooksNames } from 'modules/Contractors/Contractors.constants';
import { useFilterFields } from './useFilterFields';
import { useModal } from '../../context/modal.context';
import { StyledGrid, StyledItem, StyledRow } from 'modules/TariffSettings/styled/styled.tariffs';

interface FiltersProps {
  query: any;
  setQuery: (query: any) => void;
}

export const Filters: FC<FiltersProps> = ({ query, setQuery }) => {
  const {
    modalState, openFilters, closeModal,
  } = useModal();

  const { t } = useTranslation();

  const [form] = useForm();

  useEffect(() => {
    form.resetFields();
  }, [query, form.resetFields]);

  const filtersFields = useFilterFields();

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
        <StyledItem name={ContractorHandbooksNames.name}>
          <Input
            noBorder={false}
            onPressEnter={form.submit}
            placeholder="Поиск по имени"
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
