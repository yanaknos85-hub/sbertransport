import React from 'react';
import { Form } from 'antd';
import { FilterPanel } from 'shared/components/FilterPanel';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import { EmployeeStatus, EmployeeStatusTitle } from 'constants/constants.app';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { settingsPhoneNumber } from 'utils';
import { EmployeeHandbooksNames, EmployeeHandbookTitles } from '../Employees.constants';

export const useFilterFields = (): ModelFormFieldProps[] => [
  {
    fieldType: ModelFormFieldType.TEXT,
    name: EmployeeHandbooksNames.humanReadableId,
    label: EmployeeHandbookTitles.humanReadableId,
    editable: true,
  },
  {
    fieldType: ModelFormFieldType.TEXT,
    name: EmployeeHandbooksNames.personnelNumber,
    label: EmployeeHandbookTitles.personnelNumber,
    editable: true,
  },
  {
    fieldType: ModelFormFieldType.SELECT,
    name: EmployeeHandbooksNames.status,
    label: EmployeeHandbookTitles.status,
    editable: true,
    defaultValue: EmployeeStatus.ACTIVE,
    options: [
      {
        key: 'not selected', label: EmployeeStatusTitle.NOT_SELECTED, value: 0,
      },
      // НЕ активные - не показываем, их НЕЛЬЗЯ редактировать!
      // { key: 'removed', label: EmployeeStatusTitle.INACTIVE, value: EmployeeStatus.INACTIVE },
      {
        key: 'active', label: EmployeeStatusTitle.ACTIVE, value: EmployeeStatus.ACTIVE,
      },
    ],
  },
  {
    fieldType: ModelFormFieldType.TEXT,
    name: EmployeeHandbooksNames.fullName,
    label: EmployeeHandbookTitles.fullName,
    editable: true,
  },
  {
    fieldType: ModelFormFieldType.CUSTOM,
    name: EmployeeHandbooksNames.mobilePhone,
    component: () => (
      <Form.Item name={EmployeeHandbooksNames.mobilePhone} label={EmployeeHandbookTitles.mobilePhone}>
        <CustomInput {...settingsPhoneNumber.input} />
      </Form.Item>
    ),
    editable: true,
  },
  {
    fieldType: ModelFormFieldType.EMAIL,
    name: EmployeeHandbooksNames.email,
    label: EmployeeHandbookTitles.email,
    editable: true,
  },
];

export const Filters: React.FC<{ onApplyFilters: React.Dispatch<any> }> = ({ onApplyFilters }): JSX.Element => (
  <FilterPanel
    fields={useFilterFields()}
    onApplyFilters={onApplyFilters}
    isStatusChangeActive
  />
);
