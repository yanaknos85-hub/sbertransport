import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import Select from '../Select';
import {
  ChangedFieldsProps, Fields,
  STATUSES as CargoStatuses
} from './constants';
import { getAvailableStatuses } from './utils';

import styles from './styles.module.scss';

export const EditableField: FC<ChangedFieldsProps> = ({
  id, label, description, status, transportType,
}) => {
  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const { params: { type } } = useRouteMatch();

  const { updateRouteStatusExchange } = exchangeStore;

  const [form] = useForm<Fields>();

  const STATUSES = [...CargoStatuses];
  const options = getAvailableStatuses(transportType, status, STATUSES);

  const handleSubmit = async (value: Fields) => {
    await updateRouteStatusExchange(id, value.status, type);
  };

  const handleStatusChange = (value: string) => {
    handleSubmit({ status: value });
  };

  return (
    <Form form={form}>
      <div className={styles.selectContainer}>
        <Form.Item
          label={label}
          name="status"
          initialValue={description}
        >
          <Select
            options={options}
            onChange={handleStatusChange}
          />
        </Form.Item>
      </div>
    </Form>
  );
};
