import React, { FC, useMemo } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { EmployeeAutoCompleteProps, EmployeeBaseAutoComplete } from './EmployeeBaseAutoComplete';
import { paramsGetter } from './utils';

export const DepartmentEmployeeAutoComplete: FC<EmployeeAutoCompleteProps> = props => {
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();
  const requesterProps = useMemo(
    () => ({
      requester: employeeStore.searchDepartmentEmployees,
      paramsGetter,
    }),
    [employeeStore.searchDepartmentEmployees]
  );

  return <EmployeeBaseAutoComplete {...props} {...requesterProps} />;
};
