import React, {
  FC, useState, Suspense, useEffect
} from 'react';
import { AutoComplete } from 'antd';

import { useProfile } from 'api/profile';
import { DepartmentType } from 'modules/Planner/types';
import { useDebounce } from '../../hooks/useDebounce';
import { StyledDepartment } from './Department.style';
import { SpinWrapped } from '../../components/SpinWrapped/SpinWrapped';
import { useAppStoreContext } from '../../hooks/useAppStoreContext';

interface Props {
  index: number | undefined;
  placeholder?: string;
  fieldName?: string;
  disabled?: boolean;
}

const Department: FC<Props> = ({
  placeholder, fieldName, ...rest
}) => {
  const [searchQuery, setSearchQuery] = useState({});

  const { plannerStore } = useAppStoreContext();

  const { organizationId } = useProfile().data;

  useEffect(() => {
    plannerStore.getAllDepartmentsByOrganization(organizationId, { ...searchQuery }, { page: 0, size: 80 });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId, searchQuery]);

  const renderOptions = (dataOptions: DepartmentType[] | undefined) => dataOptions?.map((option: DepartmentType) => {
    const departmentName = `${option.departmentName}`;
    return (
      <AutoComplete.Option
        key={option.id}
        value={departmentName}
        title={departmentName}
      >
        {departmentName}
      </AutoComplete.Option>
    );
  });

  const onSearch = (text: string) => {
    if (text.length > 3) {
      setSearchQuery({ departmentName: text });
    }
  };

  const onSearchDebounce = useDebounce(onSearch, 500);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <StyledDepartment disabled={rest.disabled}>
        <AutoComplete
          className="ant-select-customize-input"
          notFoundContent={<>Имя не найдено</>}
          backfill
          allowClear
          {...rest}
          placeholder={placeholder || 'Введите название департамента'}
          // @ts-ignore
          filterOption={(inputValue, option) => option?.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1}
          onSearch={onSearchDebounce}
        >
          {renderOptions(plannerStore.departmentsListByOrg)}
        </AutoComplete>
      </StyledDepartment>
    </Suspense>
  );
};

export default Department;
