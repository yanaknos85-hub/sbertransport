import React, {
  useEffect, useState, useMemo
} from 'react';
import { Select } from 'antd';

import { useProfile } from 'api/profile';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { UUID } from 'utils/io-ts';
import { DepartmentLevels } from 'constants/constants.app';
import { useSelectDepartmentsForDepartmentLevel } from 'api/departments';
import ChevronSmall from 'shared/images/menu 2.0/ChevronSmall';

export type Props = React.ComponentProps<typeof Select> & {
  orgId?: UUID;
  numberDepartment: number;
  optionsDepartments: DepartmentLevels;
  setOptionsDepartments: React.Dispatch<React.SetStateAction<DepartmentLevels>>;
  isNewDesign?: boolean;
  organizationsValue?: string[];
  singleOrganization?: boolean;
} & {
  defaultValue?: string;
};

export const SelectDepartment = ({
  defaultValue,
  isNewDesign = false,
  orgId,
  numberDepartment,
  optionsDepartments,
  setOptionsDepartments,
  onChange,
  organizationsValue,
  singleOrganization,
  ...selectProps
}: Props): JSX.Element => {
  const { organizationId } = useProfile().data;
  const optionsDepartmentsLevels = { ...optionsDepartments, departmentLevel: numberDepartment };
  const [departmentLevel] = useSelectDepartmentsForDepartmentLevel(
    organizationsValue ? organizationsValue[0] as UUID : organizationId,
    optionsDepartmentsLevels
  );
  const [departments, setDepartments] = useState<string[] | []>([]);
  const numbersTripToOption = (searchItem: string): { label: string; value: string } => ({
    label: searchItem,
    value: searchItem,
  });
  const options = useMemo(() => departments.map(numbersTripToOption), [departments]);
  const inistialOptions = options.length ? options : [];
  const [resultOptions, setResultOptions] = useState<{ label: string; value: string }[]>([]);

  useEffect(() => {
    const filterOptions = options.length ? options : [];
    setResultOptions(filterOptions);
  }, [departments]);

  useEffect(() => {
    if (organizationsValue?.length == 1 || singleOrganization) {
      departmentLevel().then(el => setDepartments(Array.from(new Set(el))));
    }
  }, [optionsDepartments, organizationsValue]);

  const iconProps = useMemo(() => (isNewDesign ? { suffixIcon: <DownArrowIcon /> } : {}), [isNewDesign]);

  const handleChange = (value, option) => {
    if (numberDepartment) {
      setOptionsDepartments(prevDepartments => ({
        ...prevDepartments,
        [`department${numberDepartment}`]: value,
        departmentLevel: numberDepartment,
      }));
    }
    onChange && onChange(value, option);
  };

  const handleSearch = name => {
    if (!name) {
      setResultOptions(inistialOptions);
    } else {
      setResultOptions(inistialOptions.filter(el => {
        return el.label && el.label.includes(name);
      }));
    }
  };

  return (
    <Select
      {...selectProps}
      {...iconProps}
      allowClear
      autoClearSearchValue
      optionFilterProp="children"
      onSearch={handleSearch}
      showSearch
      filterOption={false}
      onChange={handleChange}
      getPopupContainer={trigger => trigger.parentNode}
      suffixIcon={(
        <div style={{ borderLeft: '1px solod grey' }}>
          <ChevronSmall />
        </div>
      )}
    >
      {resultOptions.map(({ value, label }) => (
        <Select.Option key={value} value={value}>
          <span>
            {label}
            {' '}
          </span>
        </Select.Option>
      ))}
    </Select>
  );
};

SelectDepartment.FormItem = SelectDepartment as FormItem<typeof SelectDepartment>;
