
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';
import React from 'react';

import EmployeeAutoComplete from 'shared/components/EmployeeAutoComplete';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export const TripRequestPassenger = ({ form }: { form: FormInstance }): JSX.Element => {
  const { employeeStore } = useAppStoreContext();

  const handleSelect = (passenger: EmployeeModel): void => {
    form.setFieldsValue({ passenger });
  };

  return (
    <Form.Item
      name="passenger"
      rules={[{ required: true, message: 'Поле "Кто едет" обязательно для заполнения' }]}
      label="Кто едет"
    >
      <EmployeeAutoComplete
        list={employeeStore.employeeListByOrg}
        onSelect={handleSelect}
        placeholder="Выберите пассажира"
      />
    </Form.Item>
  );
};
