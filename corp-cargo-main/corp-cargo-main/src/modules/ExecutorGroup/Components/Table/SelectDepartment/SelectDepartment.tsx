import React, {
  useCallback, useEffect, useMemo, useState, useRef
} from 'react';
import { Select } from '@sber-sbertransport/ui-kit/src';
import { Spin } from 'antd';
import debounce from 'lodash/debounce';
import { DepartmentSearchQuery, useSearchDepartment } from 'api/employee/executors-search';
import { DepartmentStatus } from 'constants/constants.app';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';
import { departmentToOption, paramsGetter } from './utils';
import { DepartmentAutoCompleteProps } from './SelectDepartment.interface';
import { Department } from 'stores/Corporate/Corporate.interface';

import styles from './SelectDepartment.module.scss';

interface ExtendedDepartmentAutoCompleteProps extends DepartmentAutoCompleteProps {
  skipInitialRequest?: boolean;
  isOpen?: boolean;
  name?: string;
  initialDepartmentIds?: UUID[];
  onDepartmentsLoad?: (departments: Department[]) => void;
}

export const SelectDepartment: React.FC<ExtendedDepartmentAutoCompleteProps> = ({
  placeholder = 'Выберите подразделение',
  debounceTimeout = 750,
  value,
  multiple = false,
  onChange,
  extraParams = {},
  minSearchLength = 9,
  clearOnBlur = true,
  className,
  skipInitialRequest = false,
  isOpen,
  initialDepartmentIds,
  onDepartmentsLoad,
  ...props
}) => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const prevSearchQueryRef = useRef<string>('');
  const prevExtraParamsRef = useRef<Record<string, unknown> | null>(null);
  const hasInitialLoadedRef = useRef(false);
  const prevInitialDepartmentIdsRef = useRef<UUID[] | undefined>(undefined);

  const [searchDepartments, { isLoading }] = useSearchDepartment();

  const stableExtraParams = useMemo(() => extraParams, [JSON.stringify(extraParams)]);
  const onSearchDepartment = useCallback((params: DepartmentSearchQuery) => {
    return searchDepartments({
      projection: 'MIN',
      status: DepartmentStatus.ACTIVE,
      ...params,
    })
      .then(data => {
        if (data) {
          setDepartments(data);
          if (onDepartmentsLoad) {
            onDepartmentsLoad(data);
          }
        }
        return data;
      })
      .catch(ignore);
  }, [searchDepartments, onDepartmentsLoad]);

  // Функция для загрузки подразделений по ID (для режима редактирования)
  const loadDepartmentsByIds = useCallback((departmentIds: UUID[]) => {
    if (departmentIds.length > 0) {
      onSearchDepartment({ departments: departmentIds });
    }
  }, [onSearchDepartment]);

  const performSearch = useCallback((search: string) => {
    if (search && search.length >= minSearchLength) {
      const searchParams = paramsGetter(search, stableExtraParams);
      onSearchDepartment(searchParams);
    } else {
      setDepartments([]);
    }
  }, [minSearchLength, stableExtraParams, onSearchDepartment]);

  const handleSearch = useMemo(
    () => debounce((search: string) => {
      setSearchQuery(search);
    }, debounceTimeout),
    [debounceTimeout]
  );

  // Эффект для сброса состояния при изменении режима (создание/редактирование)
  useEffect(() => {
    // Если initialDepartmentIds изменился с массива на undefined (переход от редактирования к созданию)
    if (prevInitialDepartmentIdsRef.current && !initialDepartmentIds) {
      setDepartments([]);
      setSearchQuery('');
      hasInitialLoadedRef.current = false;
    }
    prevInitialDepartmentIdsRef.current = initialDepartmentIds;
  }, [initialDepartmentIds]);

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
    // Если есть initialDepartmentIds (режим редактирования) и еще не загружали
    if (initialDepartmentIds && initialDepartmentIds.length > 0 && !hasInitialLoadedRef.current) {
      loadDepartmentsByIds(initialDepartmentIds);
      hasInitialLoadedRef.current = true;
    }
  }, [initialDepartmentIds, loadDepartmentsByIds]);

  // Эффект для загрузки данных при изменении value (на случай, если initialDepartmentIds не переданы)
  useEffect(() => {
    if (!skipInitialRequest && value && value.length > 0 && departments.length === 0 && !isDropdownOpen) {
      // Загружаем подразделения по ID из value
      const idsToLoad = value.filter(id => !departments.some(dept => dept.id === id));
      if (idsToLoad.length > 0) {
        loadDepartmentsByIds(idsToLoad);
      }
    }
  }, [skipInitialRequest, value, departments.length, isDropdownOpen, loadDepartmentsByIds]);

  const options = useMemo(() => departments.map(departmentToOption), [departments]);

  const handleFocus = () => {
    setIsDropdownOpen(true);
    if (searchQuery && searchQuery.length >= minSearchLength) {
      performSearch(searchQuery);
    }
  };

  const handleDropdownVisibleChange = (open: boolean) => {
    setIsDropdownOpen(open);
    if (!open && clearOnBlur) {
      setDepartments([]);
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
