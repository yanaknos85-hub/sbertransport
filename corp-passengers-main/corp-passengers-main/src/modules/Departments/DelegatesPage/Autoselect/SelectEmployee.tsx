import React, { useCallback, useEffect, useState } from 'react';

import { debounce } from '@material-ui/core';
import { Select, Spin } from 'antd';
import { useProfile } from 'api/profile';
import { useTranslation } from 'i18n';

import { DepartmentsAllResponse } from 'stores/Department/Department.interface';
import { Employee } from 'stores/Employee/Employee.interface';
import { UUID } from 'utils/io-ts';
import { useGetEmployees } from 'api/delegates';
import { formatName } from 'utils/formatName';

export type Props = React.ComponentProps<typeof Select> & {
  restrict?: UUID[];
  isRedistribution?: boolean;
  orgId?: UUID;
} & {
  defaultValue?: string;
} & {
  showId?: boolean;
};

export const SelectEmployee = ({
  restrict,
  defaultValue,
  isRedistribution,
  showId = false,
  orgId,
  onChange,
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

  const { organizationId } = useProfile().data;
  // @ts-ignore
  const [fetchDepartments] = useGetEmployees(orgId || organizationId);

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
    fetchDepartments({
      page, size: 20, departmentName,
    }).then(responseDepartments => {
      setDepartments(oldDepartments => ({
        ...oldDepartments,
        ...Object.assign({}, ...(responseDepartments?.content?.map((dep: Employee) => ({ [dep.id]: dep })) ?? [])),
      }));
      setIsFetching(false);
      setTotalPages(responseDepartments?.totalPages ?? Infinity);
    });
  }, [page, fetchDepartments, departmentName]);

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

  const clearDepartmentName = () => {
    // данный код является рудементом, но решил его оставить т к копировал и может он для чего то пригодиться, баг правиться если мы не заходим сюда
    return;
    if (departmentName === '') {
      return;
    }
    setDepartmentName('');
    setDepartments({});
  };

  const allDepartments: DepartmentsAllResponse = Object.values(departments);

  return (
    <Select
      {...selectProps}
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
      {allDepartments
        ?.filter(({ id }) => (restrict ? restrict.includes(id) : true))
        .map(args => {
          const {
            id, humanReadableId, firstName, patronymic, lastName, personnelNumber,
          } = args;

          return (
            <Select.Option key={id} value={id}>
              {showId && (
              <span>
                [
                {humanReadableId}
                ]
                {' '}
              </span>
              )}
              {`${formatName({
                firstName: firstName as string,
                patronymic,
                lastName: lastName as string,
              })} (${personnelNumber})`}
            </Select.Option>
          );
        })}
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

SelectEmployee.FormItem = SelectEmployee as FormItem<typeof SelectEmployee>;
