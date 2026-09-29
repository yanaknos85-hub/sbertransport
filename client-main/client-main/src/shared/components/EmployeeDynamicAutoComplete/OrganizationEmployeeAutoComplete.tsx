import React, { FC, useMemo } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { EmployeeAutoCompleteProps, EmployeeBaseAutoComplete } from './EmployeeBaseAutoComplete';
import { paramsGetter } from './utils';

export const OrganizationEmployeeAutoComplete: FC<EmployeeAutoCompleteProps> = props => {
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();
  const requesterProps = useMemo(
    () => ({
      requester: employeeStore.searchOrganizationEmployees,
      paramsGetter,
    }),
    [employeeStore.searchOrganizationEmployees]
  );

  return <EmployeeBaseAutoComplete {...props} {...requesterProps} />;
};
