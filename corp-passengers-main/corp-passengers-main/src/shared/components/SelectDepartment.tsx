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

  const [page, setPage] = useState(0);
  const [departments, setDepartments] = useState<Record<DepartmentsAllResponse[0]['id'], DepartmentsAllResponse[0]>>(
    {}
  );
  const [isFetching, setIsFetching] = useState(false);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [departmentName, setDepartmentName] = useState('');
  const [selectedLabel, setSelectedLabel] = useState<string | undefined>();

  const { organizationId } = useProfile().data;
  // @ts-ignore
  const [fetchDepartments] = useSelectDepartmentsSearch(orgId || organizationId);

  // поскольку это часто используемый компонент решил не удалять старую логику и просто добавить свою дабы не поломать где-нибудь
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
    fetchDepartments({ page, departmentName: departmentName || undefined }).then(responseDepartments => {
      setDepartments(oldDepartments => ({
        ...oldDepartments,
        ...Object.assign({}, ...(responseDepartments?.content?.map(dep => ({ [dep.id]: dep })) ?? [])),
      }));
      setIsFetching(false);
      setTotalPages(responseDepartments?.totalPages ?? Infinity);
    });
  }, [page, fetchDepartments, departmentName]);

  const iconProps = useMemo(() => (isNewDesign ? { suffixIcon: <DownArrowIcon /> } : {}), [isNewDesign]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const handleEnterDepartmentName = useCallback(
    debounce((name: string) => {
      setTotalPages(Infinity);
      setDepartments({});
      setPage(0);
      setDepartmentName(name);
      setIsFetching(true);
    }, 500),
    []
  );

  const handleSelect = value => {
    const selected = departments[value];
    const label = selected?.fullStructurePath || selected?.departmentName;
    if (label) {
      setSelectedLabel(label);
    }

    if (departmentName === '') {
      return;
    }
    setDepartmentName('');
    setDepartments({});
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
          .map(({ id, departmentName }) => (
            <Select.Option key={id} value={id}>
              {departmentName}
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
      onSelect={handleSelect}
      onChange={onChange}
    >
      {selectProps.value && !allDepartments.some(x => x.id === selectProps.value) && (
        <Select.Option value={selectProps.value as string}>{selectedLabel || label}</Select.Option>
      )}
      {allDepartments
        ?.filter(({ id }) => (restrict ? restrict.includes(id) : true))
        .map(({
          id, humanReadableId, departmentName, fullStructurePath,
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
            {fullStructurePath || departmentName}
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
