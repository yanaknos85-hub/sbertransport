import React, { FC, useEffect, useState } from 'react';
import { IconButton } from 'shared/components/IconButton';
import { Icon } from 'shared/components/Icon';
import { Modal } from 'shared/components/Modal/Modal';
import { useForm } from 'antd/lib/form/Form';
import { Form } from 'antd';
import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';
import { ContractorsFilters } from 'stores/Contractors/Contractors.interface';
import { Button } from 'shared/components/Button/Button';
import { useTranslation } from 'i18n';
import { useFilterFields } from './useFilterFields';
import { useModal } from '../context/modal.context';
import {
  StyledFlex, StyledTitle, StyledBlockTitle, StyledRow
} from '../styled.table';
import { UUID } from 'utils/io-ts';

interface FiltersProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  query: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setQuery: (query: any) => void;
}

export const Filters: FC<FiltersProps> = ({ query, setQuery }) => {
  const [orgExecId, setOrgExecId] = useState('');
  const [orgId, setOrgId] = useState([]);

  const {
    modalState, openFilters, closeModal,
  } = useModal();

  const { t } = useTranslation();

  const [form] = useForm();

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, form.resetFields]);

  const { parameterFields, customerFields } = useFilterFields(orgExecId as UUID, orgId as UUID[]);

  const onSearch = (values: ContractorsFilters) => {
    setQuery({
      ...values,
    });
    form.resetFields();
    closeModal();
  };

  const resetFields = () => {
    setQuery({});
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
            <Button type="primary" onClick={form.submit}>
              {t.global.confirm}
            </Button>,
          ]}
        >
          <StyledTitle>Фильтры</StyledTitle>
          <StyledFlex>
            <StyledBlockTitle>По заказчику</StyledBlockTitle>
            {customerFields.map(field => {
              return field.name === 'customerOrganizations'
                ? (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                    onChange={val => {
                      setOrgId(val);
                      form.resetFields(['customerDepartments']);
                    }}
                  />
                )
                : (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                );
            }
            )}
            <StyledBlockTitle>По исполнителю</StyledBlockTitle>
            {parameterFields.map(field => {
              return field.name === 'executorOrganization'
                ? (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                    onChange={val => {
                      setOrgExecId(val);
                      form.resetFields(['executorPersonnelNumber']);
                    }}
                  />
                )
                : (
                  <ModelFormField
                    key={field.name}
                    {...field}
                    form={form}
                  />
                );
            }
            )}
          </StyledFlex>
        </Modal>
      </StyledRow>
    </Form>
  );
};
