import React, { FC, useMemo } from 'react';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { EmployeeAutoCompleteProps, EmployeeBaseAutoComplete } from './EmployeeBaseAutoComplete';
import { paramsGetter } from './utils';

export const DelegateCandidateAutoComplete: FC<EmployeeAutoCompleteProps> = props => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();
  const requesterProps = useMemo(
    () => ({
      requester: delegatesStore.searchSelfDelegateCandidates,
      paramsGetter,
    }),
    [delegatesStore.searchSelfDelegateCandidates]
  );

  return <EmployeeBaseAutoComplete {...props} {...requesterProps} />;
};
