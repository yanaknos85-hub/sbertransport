import React, {
  useCallback, useEffect, useMemo, useState, useRef
} from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { Spin } from 'antd';
import debounce from 'lodash/debounce';
import { EmployeeSearchQuery, useSearchEmployee } from 'api/employee/executors-search';
import { EmployeeStatus } from 'constants/constants.app';
import { Employee } from 'stores/Employee/Employee.interface';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';
import { employeeToOption, paramsGetter } from './utils';
import { EmployeeAutoCompleteProps } from './SelectEmployee.interface';

import styles from './SelectEmployee.module.scss';

interface ExtendedEmployeeAutoCompleteProps extends EmployeeAutoCompleteProps {
  skipInitialRequest?: boolean;
  isOpen?: boolean;
  name?: string;
  initialEmployeeIds?: UUID[];
}

export const SelectEmployee: React.FC<ExtendedEmployeeAutoCompleteProps> = ({
  placeholder = 'Выберите сотрудника',
  debounceTimeout = 750,
  value,
  multiple = false,
  onChange,
  extraParams = {},
  minSearchLength = 3,
  clearOnBlur = true,
  className,
  skipInitialRequest = false,
  isOpen,
  initialEmployeeIds,
  ...props
}) => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const prevSearchQueryRef = useRef<string>('');
  const prevExtraParamsRef = useRef<Record<string, unknown> | null>(null);
  const hasInitialLoadedRef = useRef(false);
  const prevInitialEmployeeIdsRef = useRef<UUID[] | undefined>(undefined);

  const [searchEmployee, { isLoading }] = useSearchEmployee();

  const stableExtraParams = useMemo(() => extraParams, [JSON.stringify(extraParams)]);

  const onSearchEmployee = useCallback((params: EmployeeSearchQuery) => {
    return searchEmployee({
      projection: 'MIN',
      status: EmployeeStatus.ACTIVE,
      ...params,
    })
      .then(data => {
        if (data?.content) {
          setEmployees(data.content);
        }
        return data;
      })
      .catch(ignore);
  }, [searchEmployee]);

  // Функция для загрузки сотрудников по ID (для режима редактирования)
  const loadEmployeesByIds = useCallback((employeeIds: UUID[]) => {
    if (employeeIds.length > 0) {
      onSearchEmployee({ employees: employeeIds });
    }
  }, [onSearchEmployee]);

  // Основная функция поиска
  const performSearch = useCallback((search: string) => {
    if (search && search.length >= minSearchLength) {
      const searchParams = paramsGetter(search, stableExtraParams);
      onSearchEmployee(searchParams);
    } else {
      setEmployees([]);
    }
  }, [minSearchLength, stableExtraParams, onSearchEmployee]);

  const handleSearch = useMemo(
    () => debounce((search: string) => {
      setSearchQuery(search);
    }, debounceTimeout),
    [debounceTimeout]
  );

  // Эффект для сброса состояния при изменении режима (создание/редактирование)
  useEffect(() => {
    // Если initialEmployeeIds изменился с массива на undefined (переход от редактирования к созданию)
    if (prevInitialEmployeeIdsRef.current && !initialEmployeeIds) {
      setEmployees([]);
      setSearchQuery('');
      hasInitialLoadedRef.current = false;
    }
    prevInitialEmployeeIdsRef.current = initialEmployeeIds;
  }, [initialEmployeeIds]);

  // Эффект для выполнения поиска при изменении searchQuery
  useEffect(() => {
    if (searchQuery !== prevSearchQueryRef.current
      || JSON.stringify(stableExtraParams) !== JSON.stringify(prevExtraParamsRef.current)) {
      performSearch(searchQuery);
      prevSearchQueryRef.current = searchQuery;
      prevExtraParamsRef.current = stableExtraParams;
    }
  }, [searchQuery, stableExtraParams, performSearch]);

  // Эффект для начальной загрузки данных в режиме редактирования
  useEffect(() => {
    // Если есть initialEmployeeIds (режим редактирования) и еще не загружали
    if (initialEmployeeIds && initialEmployeeIds.length > 0 && !hasInitialLoadedRef.current) {
      loadEmployeesByIds(initialEmployeeIds);
      hasInitialLoadedRef.current = true;
    }
  }, [initialEmployeeIds, loadEmployeesByIds]);

  // Эффект для загрузки данных при изменении value (на случай, если initialEmployeeIds не переданы)
  useEffect(() => {
    if (!skipInitialRequest && value && value.length > 0 && employees.length === 0 && !isDropdownOpen) {
      // Загружаем сотрудников по ID из value
      const idsToLoad = value.filter(id => !employees.some(emp => emp.id === id));
      if (idsToLoad.length > 0) {
        loadEmployeesByIds(idsToLoad);
      }
    }
  }, [skipInitialRequest, value, employees.length, isDropdownOpen, loadEmployeesByIds]);

  const options = useMemo(() => employees.map(employeeToOption), [employees]);

  const handleFocus = () => {
    setIsDropdownOpen(true);
    if (searchQuery && searchQuery.length >= minSearchLength) {
      performSearch(searchQuery);
    }
  };

  const handleDropdownVisibleChange = (open: boolean) => {
    setIsDropdownOpen(open);
    if (!open && clearOnBlur) {
      setEmployees([]);
      setSearchQuery('');
    }
  };

  const selectClassName = `${styles.customSelect} ${className || ''}`.trim();

  return (
    <Select
      placeholder={placeholder}
      allowClear
      showSearch
      mode={multiple ? 'multiple' : undefined}
      value={value}
      onSearch={handleSearch}
      notFoundContent={isLoading ? <Spin size="small" /> : null}
      onFocus={handleFocus}
      onDropdownVisibleChange={handleDropdownVisibleChange}
      className={selectClassName}
      {...props}
      onChange={onChange}
      options={options}
      filterOption={false}
    />
  );
};
