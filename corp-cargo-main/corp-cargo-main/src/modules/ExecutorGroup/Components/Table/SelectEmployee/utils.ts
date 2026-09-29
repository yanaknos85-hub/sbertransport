import { LabeledValue } from 'antd/es/select';
import { RequestParams } from '@sber-sbertransport/mf-core';

import { Employee } from 'stores/Employee/Employee.interface';

export const fullNameWithCode = ({
  firstName,
  patronymic,
  lastName,
  personnelNumber,
}: {
  firstName: string;
  patronymic?: string;
  lastName: string;
  personnelNumber?: string;
}): string => [firstName, patronymic, lastName, (personnelNumber && `(${personnelNumber})`)].filter(Boolean).join(' ');

export const paramsGetter = (search: string, extraParams?: RequestParams): RequestParams => {
  const param = /^[0-9]+$/.test(search) ? 'personnelNumber' : 'fullName';
  return { ...(extraParams || {}), [param]: search };
};

export const employeeToOption = (employee: Employee): LabeledValue => ({
  value: employee.id,
  label: fullNameWithCode(employee),
  ...employee,
});
