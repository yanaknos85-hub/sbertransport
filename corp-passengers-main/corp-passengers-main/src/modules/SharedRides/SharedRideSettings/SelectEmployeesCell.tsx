import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Select } from 'antd';
import { UUID } from 'utils/io-ts';

import { useProfile } from 'api/profile';
import { useAllEmployees } from 'api/employee';
import { LabeledValue } from 'utils/Types';
import { startsWithIgnoreCase } from 'utils';
import { Employee } from 'stores/Employee/Employee.interface';
import { nameWithInitials } from 'utils/employee';
import { EditableCellProps } from 'shared/components/EditableTable';

import { SettingsRecord } from '.';

import styles from './styles.module.scss';

type SelectOption = LabeledValue<UUID> & { employee: Employee; employeeNumber: string };

export const SelectEmployeesCell: FC<EditableCellProps<SettingsRecord, SettingsRecord['employees']>> = observer(
  ({
    cellValue, isEditingRecord, onChange,
  }) => {
    const profile = useProfile().data;

    const selectedEmployees = (cellValue || []).map(({ id }) => id);

    // @ts-ignore
    const { content: employees } = useAllEmployees(profile.organizationId).data.employeeResponse;

    if (isEditingRecord) {
      const handleChange = (employeeIds: UUID[]) => onChange({ employees: employeeIds.map(id => ({ id })) });

      const options: SelectOption[] = employees.map(employee => ({
        value: employee.id as UUID,
        label: nameWithInitials(employee),
        // eslint-disable-next-line no-use-before-define
        employeeNumber: formatEmployee(employee),
        employee,
      }));

      return (
        <Select
          mode="multiple"
          className={styles.cellSelect}
          allowClear
          value={selectedEmployees}
          onChange={handleChange}
          options={options}
          optionLabelProp="label"
          // eslint-disable-next-line no-use-before-define
          filterOption={filterOption}
        />
      );
    }

    return (
      <div className={styles.cellDisplay}>
        {employees
          .filter(employee => selectedEmployees.includes(employee.id as UUID))
          .map(nameWithInitials)
          .join(', ')}
      </div>
    );
  }
);

const formatEmployee = (employee: Employee): string => `${employee.personnelNumber}`;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const filterOption = (inputValue: string, o: any): boolean => {
  const {
    firstName, lastName,
  } = (o as SelectOption).employee;
  const filterValue = inputValue.trim();
  return (
    filterValue.length === 0
    || startsWithIgnoreCase(firstName, filterValue)
    || startsWithIgnoreCase(lastName, filterValue)
    || startsWithIgnoreCase('', filterValue)
    || startsWithIgnoreCase(`${firstName} ${lastName}`, filterValue)
    || startsWithIgnoreCase(`${lastName} ${firstName}`, filterValue)
  );
};
