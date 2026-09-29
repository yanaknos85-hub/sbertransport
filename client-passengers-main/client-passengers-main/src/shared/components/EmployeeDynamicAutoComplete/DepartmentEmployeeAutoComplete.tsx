import React, { FC, useMemo } from 'react';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { EmployeeAutoCompleteProps, EmployeeBaseAutoComplete } from './EmployeeBaseAutoComplete';
import { paramsGetter } from './utils';

export const DepartmentEmployeeAutoComplete: FC<EmployeeAutoCompleteProps> = props => {
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.employeeExtStore]: employeeExtStore,
  } = useAppStoreContext();
  const requesterProps = useMemo(
    () => ({
      requester: employeeExtStore.searchEmployeesByName as any,
      paramsGetter,
    }),
    [employeeStore.searchDepartmentEmployees]
  );

  return <EmployeeBaseAutoComplete {...props} {...requesterProps} />;
};
