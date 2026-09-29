import { IEmployeeStore } from '@sber-sbertransport/mf-core';
import { AutoComplete, AutoCompleteProps, Spin } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { StyledEmployee } from './Employee.style';

type Props = AutoCompleteProps & { index: number | undefined; placeholder?: string };

const Employee: FC<Props> = observer(({
  index = 0, placeholder, ...rest
}) => {
  const { [StoreNames.employeeStore]: employeeStore }: { employeeStore: IEmployeeStore } = useAppStoreContext();
  const {
    employeeAutocompleteList, findEmployees, onEmployeeSelect,
  } = employeeStore;
  const [fetching, setFetching] = React.useState(false);

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
          findEmployees(value as any);
          if (rest.onSearch) {
            rest.onSearch(value);
          }
        }}
        // @ts-ignore
        onSelect={(value, option) => {
          setFetching(false);
          onEmployeeSelect(value, index);
          if (rest.onSelect) {
            rest.onSelect(value, option);
          }
        }}
      >
        {employeeAutocompleteList.map(({ fullNameWithCode }) => (
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
