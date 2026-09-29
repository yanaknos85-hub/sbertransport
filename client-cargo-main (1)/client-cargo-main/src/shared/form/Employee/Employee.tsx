import React, { FC, useEffect, useState } from 'react';
import { AutoComplete, AutoCompleteProps, Spin } from 'antd';
import { DefaultOptionType } from 'antd/es/select';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import { StyledEmployee } from './Employee.style';

type Props = AutoCompleteProps & { index: number | undefined; placeholder?: string };

const Employee: FC<Props> = observer(({
  index = 0, placeholder, ...rest
}) => {
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();

  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  const {
    employeeAutocompleteList, findEmployees, onEmployeeSelect,
  } = employeeStore;
  const [fetching, setFetching] = React.useState(false);
  const [options, setOptions] = useState(employeeAutocompleteList);

  useEffect(() => {
    setOptions(employeeAutocompleteList);
  }, [employeeAutocompleteList]);

  return (
    <StyledEmployee>
      <AutoComplete
        backfill={true}
        allowClear={true}
        className="ant-select-customize-input"
        notFoundContent={fetching ? <Spin size="small" /> : <>Имя не найдено</>}
        {...rest}
        placeholder={placeholder || 'Введите имя получателя'}
        onSearch={value => {
          if (value.length > 3) {
            setFetching(true);
          }
          findEmployees(value);
          if (rest.onSearch) {
            rest.onSearch(value);
          }
          setTimeout(() => setFetching(false), 3000);
        }}
        onFocus={() => setOptions([])}
        onSelect={(value: string, option: DefaultOptionType) => {
          setFetching(false);
          onEmployeeSelect(value, index);
          if (rest.onSelect) {
            rest.onSelect(value, option);
          }
        }}
      >
        {(!IS_PERSONAL_DEVICE ? options : options.slice(0, 5)).map(({ fullNameWithCode }) => (
          <AutoComplete.Option
            value={fullNameWithCode}
            key={fullNameWithCode}
            title={fullNameWithCode}
          >
            {fullNameWithCode}
          </AutoComplete.Option>
        ))}
      </AutoComplete>
    </StyledEmployee>
  );
});

export default Employee;
