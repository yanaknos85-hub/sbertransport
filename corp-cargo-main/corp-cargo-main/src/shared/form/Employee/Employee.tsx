import React, {
  FC, useState, Suspense, useEffect
} from 'react';
import { AutoComplete } from 'antd';

import { useProfile } from 'api/profile';
import { Employee as IEmployee } from 'stores/Employee/Employee.interface';
import { useDebounce } from '../../hooks/useDebounce';
import { StyledEmployee } from './Employee.style';
import { useAppStoreContext } from '../../hooks/useAppStoreContext';
import { SpinWrapped } from '../../components/SpinWrapped/SpinWrapped';

interface Props {
  index: number | undefined;
  placeholder?: string;
  disabled?: boolean;
}

interface Query {
  fullName?: string;
}

const Employee: FC<Props> = ({
  placeholder, ...rest
}) => {
  const [searchQuery, setSearchQuery] = useState<Query>({});

  const { plannerStore } = useAppStoreContext();

  const { organizationId } = useProfile().data;

  useEffect(() => {
    plannerStore.getAllEmployeesByOrganization(organizationId, { ...searchQuery }, { page: 0, size: 20 });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId, searchQuery]);

  const renderOptions = (dataOptions: IEmployee[] | undefined) => dataOptions?.map((option: IEmployee) => {
    const fullName = `${option.lastName} ${option.firstName} ${option.patronymic || ''} ${option.personnelNumber}`;
    return (
      <AutoComplete.Option
        key={option.id}
        value={fullName}
        title={option.id}
      >
        {fullName}
      </AutoComplete.Option>
    );
  });

  const onSearch = (text: string) => {
    if (text.length > 3) {
      setSearchQuery({ fullName: text });
    }
  };
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const onSelect = (val: any, option: any) => {
    plannerStore.setEmployeeId(option.key);
  };

  const onSearchDebounce = useDebounce(onSearch, 500);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <StyledEmployee disabled={rest.disabled}>
        <AutoComplete
          className="ant-select-customize-input"
          notFoundContent={<>Имя не найдено</>}
          backfill
          allowClear
          {...rest}
          placeholder={placeholder || 'Введите имя'}
          filterOption={(inputValue, option) => option?.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1}
          onSearch={onSearchDebounce}
          onSelect={onSelect}
        >
          {renderOptions(plannerStore.employeeListByOrg)}
        </AutoComplete>
      </StyledEmployee>
    </Suspense>
  );
};

export default Employee;
