import React, {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import debounce from 'lodash/debounce';
import { Spin } from 'antd';

import { EmployeeSearchQuery, useSearchEmployeeMutation } from 'api/employee/search';
import { EmployeeStatus } from 'constants/constants.app';
import { Employee } from 'stores/Employee/Employee.interface';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';

import { employeeToOption, paramsGetter } from './utils';
import { EmployeeAutoCompleteProps } from './SelectEmployee.interface';

export const SelectEmployee: React.FC<EmployeeAutoCompleteProps> = ({
  placeholder = 'Выберите сотрудника',
  debounceTimeout = 750,
  value,
  multiple = false,
  filterOption = false,
  onChange,
  extraParams = {},
  minSearchLength = 3,
  clearOnBlur,
  ...props
}) => {
  const [selected, setSelected] = useState<UUID[]>();
  const [employees, setEmployees] = useState<Employee[]>([]);

  const [searchEmployee, { isLoading }] = useSearchEmployeeMutation();

  useEffect(() => {
    setSelected(value);
  }, [value]);

  const onSearchEmployee = useCallback((params: EmployeeSearchQuery) => {
    searchEmployee({
      projection: 'MIN', status: EmployeeStatus.ACTIVE, ...params,
    })
      .then(data => data?.content && setEmployees(data.content))
      .catch(ignore);
  }, [searchEmployee]);

  const handleSearch = useMemo(
    () => debounce((search: string) => {
      if (search && search.length >= minSearchLength) {
        onSearchEmployee({ ...paramsGetter(search, extraParams) });
      }
    }, debounceTimeout),
    [debounceTimeout, minSearchLength, onSearchEmployee, extraParams]
  );

  useEffect(() => {
    if (value?.length && !employees.length) {
      onSearchEmployee({ employees: value });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const options = useMemo(() => employees.map(employeeToOption), [employees]);

  const onBlur = () => {
    if (clearOnBlur) {
      setEmployees([]);
    }
  };

  return (
    <Select
      placeholder={placeholder}
      allowClear
      showSearch
      filterOption={filterOption}
      mode={multiple ? 'multiple' : undefined}
      value={selected}
      onSearch={handleSearch}
      onChange={onChange}
      notFoundContent={isLoading ? <Spin size="small" /> : null}
      onBlur={onBlur}
      {...props}
      options={options}
    />
  );
};
