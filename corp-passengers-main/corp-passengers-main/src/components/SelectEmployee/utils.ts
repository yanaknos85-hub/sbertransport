import { LabeledValue } from 'antd/es/select';
import { RequestParams } from '@sber-sbertransport/mf-core';

import { Employee } from 'stores/Employee/Employee.interface';
import { fullNameWithCode } from 'utils/employee';

export const paramsGetter = (search: string, extraParams?: RequestParams): RequestParams => {
  const param = /^[0-9]+$/.test(search) ? 'personnelNumber' : 'fullName';
  return { ...(extraParams || {}), [param]: search };
};

export const employeeToOption = (employee: Employee): LabeledValue => ({
  value: employee.id,
  label: fullNameWithCode(employee),
  ...employee,
});
