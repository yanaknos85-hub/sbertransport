import { EmployeeModel, EmployeeDetailedModel } from '@sber-sbertransport/mf-core';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { useState, useEffect } from 'react';

import { EmployeeStatus } from 'constants/constants.app';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { IDepartment } from 'stores/Corporate/Corporate.interface';
import { StoreNames } from 'stores/StoreNames.enum';

interface IUseProfilePage {
  onFinish(values: any): void;
  form: FormInstance;
  selfDetailed: EmployeeDetailedModel;
  isDisabled: boolean;
  editableFields: Record<string, boolean> | undefined;
  setEditableFields: React.Dispatch<React.SetStateAction<Record<string, boolean> | undefined>>;
  selfDepartment: IDepartment | undefined;
}

export const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

export const useProfilePage = (): IUseProfilePage => {
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.mappedStore]: mappedStore,
    [StoreNames.corporateStore]: corporateStore,
  } = useAppStoreContext();

  const { editEmployee, editPhone } = employeeStore;
  const { selfEmployee } = selfStore;

  useEffect(() => {
    corporateStore.loadOrganization(selfEmployee.organizationId);
    corporateStore.loadDepartment(selfEmployee.organizationId, selfEmployee.departmentId);
    corporateStore.loadAllPositions(selfEmployee.organizationId);
  }, []);

  const selfDepartment = corporateStore.department;

  const [editableFields, setEditableFields] = useState<Record<string, boolean>>();

  const [form] = Form.useForm();

  const isDisabled = !(editableFields && Object.values(editableFields).find(field => field)) || !editableFields;

  function onFinish(values: any): void {
    const mobilePhone = clearPhone(values.mobilePhone) ?? '';

    if (!mobilePhone) {
      setEditableFields({});
      return;
    }

    const model = new EmployeeModel({
      ...selfEmployee,
      ...values,
      mobilePhone,
      status: EmployeeStatus.ACTIVE,
    });

    // Если меняется только телефон - обновляем его через patch
    // ToDo: пересмотреть эту логигу. Вытащить телефон в отдельную форму
    if (Object.keys(values).length === 1 && Object.prototype.hasOwnProperty.call(values, 'mobilePhone')) {
      editPhone(mobilePhone);
    } else {
      editEmployee(model);
    }

    setEditableFields({});
  }

  return {
    onFinish,
    form,
    isDisabled,
    editableFields,
    setEditableFields,
    selfDetailed: mappedStore.selfEmployeeDetailed,
    selfDepartment,
  };
};
