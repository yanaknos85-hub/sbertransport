import React, {
  useCallback, useEffect, useState, useMemo
} from 'react';

import { debounce } from '@material-ui/core';
import * as R from 'ramda';
import { Select, Spin } from 'antd';
import { useTranslation } from 'i18n';

import { useSelectDepartmentsSearch } from 'api/departments';
import { useProfile } from 'api/profile';
import { DepartmentsAllResponse } from 'stores/Department/Department.interface';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { UUID } from 'utils/io-ts';

interface Department {
  code: string;
  departmentName: string;
  id: UUID;
}
/* Компонент скопирован из shared и немного доработал, для Исполнения заявок */
export type Props = React.ComponentProps<typeof Select> & {
  restrict?: UUID[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  childLimits?: any[];
  isRedistribution?: boolean;
  orgId?: UUID;
  isNewDesign?: boolean;
} & {
  defaultValue?: string;
  label?: string;
} & {
  showId?: boolean;
};

// Глобальный кэш, который живет пока пользователь не обновит страницу браузера.
// Ключом будет ID организации, значением - список департаментов.
const DEPARTMENTS_CACHE: Record<string, DepartmentsAllResponse> = {};

export const SelectDepartment = ({
  restrict,
  childLimits,
  defaultValue,
  isRedistribution,
  showId = false,
  isNewDesign = false,
  orgId,
  onChange,
  label,
  ...selectProps
}: Props): JSX.Element => {
  const { t } = useTranslation();
  const { organizationId } = useProfile().data;

  // Определяем ключ кэширования (текущая организация)
  const currentOrgId = orgId || organizationId;
  const [departments, setDepartments] = useState<Record<string, DepartmentsAllResponse[0]>>(
    () => {
      if (currentOrgId && DEPARTMENTS_CACHE[currentOrgId]) {
        // Преобразуем массив из кэша обратно в Record формат, который использует компонент
        return Object.assign({}, ...DEPARTMENTS_CACHE[currentOrgId].map(dep => ({ [dep.id]: dep })));
      }
      return {};
    }
  );

  const [page, setPage] = useState(0);
  const [isFetching, setIsFetching] = useState(false);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [departmentName, setDepartmentName] = useState('');

  const [fetchDepartments] = useSelectDepartmentsSearch(currentOrgId);

  function getDepartmentsArray(): Department[] {
    const depsArray: Department[] = [];
    childLimits?.forEach(child => depsArray.push(child.department));
    return depsArray;
  }

  const handleScroll = useCallback(
    e => {
      const target = e.target as HTMLDivElement;
      if (isFetching || page >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + 200 < target.scrollHeight) {
        return;
      }

      setPage(p => p + 1);
      setIsFetching(true);
    },
    [page, totalPages, isFetching]
  );

  useEffect(() => {
    // Если мы на первой странице, не ищем по имени и у нас уже есть закэшированные данные для этой организации
    // То мы пропускаем запрос.
    if (
      page === 0 &&
      !departmentName &&
      currentOrgId &&
      DEPARTMENTS_CACHE[currentOrgId] &&
      DEPARTMENTS_CACHE[currentOrgId].length > 0
    ) {
      // Данные уже загружены из кэша в useState при инициализации, ничего делать не нужно
      return;
    }

    fetchDepartments({ page, departmentName: departmentName || undefined }).then(responseDepartments => {
      // Сохраняем полученные данные в кэш, если это первая страница общего списка
      if (page === 0 && !departmentName && responseDepartments?.content && currentOrgId) {
        DEPARTMENTS_CACHE[currentOrgId] = responseDepartments.content;
      }

      setDepartments(oldDepartments => ({
        ...oldDepartments,
        ...Object.assign({}, ...(responseDepartments?.content?.map(dep => ({ [dep.id]: dep })) ?? [])),
      }));
      setIsFetching(false);
      setTotalPages(responseDepartments?.totalPages ?? Infinity);
    });
  }, [page, fetchDepartments, departmentName, currentOrgId]);

  const iconProps = useMemo(() => (isNewDesign ? { suffixIcon: <DownArrowIcon /> } : {}), [isNewDesign]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const handleEnterDepartmentName = useCallback(
    debounce((name: string) => {
      // При поиске мы должны игнорировать кэш и идти на бэкенд
      setTotalPages(Infinity);
      setDepartments({});
      setPage(0);
      setDepartmentName(name);
      setIsFetching(true);
    }, 500),
    []
  );

  const clearDepartmentName = () => {
    if (departmentName === '') {
      return;
    }
    setDepartmentName('');

    // При очистке поиска возвращаем данные из кэша, если они есть
    if (currentOrgId && DEPARTMENTS_CACHE[currentOrgId]) {
      setDepartments(Object.assign({}, ...DEPARTMENTS_CACHE[currentOrgId].map(dep => ({ [dep.id]: dep }))));
    } else {
      setDepartments({});
      // Триггерим эффект для загрузки первой страницы заново, если кэша нет
      setPage(0);
    }
  };

  const allDepartments: DepartmentsAllResponse = Object.values(departments);

  if (getDepartmentsArray() && isRedistribution) {
    return (
      <Select
        {...selectProps}
        {...iconProps}
        defaultValue={defaultValue}
        onChange={onChange}
      >
        {getDepartmentsArray()
          .filter(restrict ? ({ id }) => restrict.includes(id) : R.always(true))
          .map(({ id, departmentName: name }) => (
            <Select.Option key={id} value={id}>
              {name}
            </Select.Option>
          ))}
      </Select>
    );
  }

  return (
    <Select
      {...selectProps}
      {...iconProps}
      allowClear
      autoClearSearchValue
      optionFilterProp="children"
      value={selectProps.value as string}
      defaultValue={defaultValue}
      onPopupScroll={handleScroll}
      onSearch={handleEnterDepartmentName}
      showSearch
      filterOption={false}
      onSelect={clearDepartmentName}
      onChange={onChange}
    >
      {!allDepartments.some(x => x.id === selectProps.id) && selectProps.value && label && (
        <Select.Option value={selectProps.value as string}>{label}</Select.Option>
      )}
      {allDepartments
        ?.filter(({ id }) => (restrict ? restrict.includes(id) : true))
        .map(({
          id, humanReadableId, departmentName: name, fullStructurePath,
        }) => (
          <Select.Option key={id} value={id}>
            {showId && (
              <span>
                [
                {humanReadableId}
                ]
                {' '}
              </span>
            )}
            {fullStructurePath || name}
          </Select.Option>
        ))}
      {isFetching && (
        <Select.Option
          key="loading"
          disabled
          value=""
        >
          {t.global.load}
          {' '}
          ...
          <Spin />
        </Select.Option>
      )}
    </Select>
  );
};

SelectDepartment.FormItem = SelectDepartment as FormItem<typeof SelectDepartment>;
