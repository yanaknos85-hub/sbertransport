import { useEffect, useState } from 'react';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { useGetDelegates } from 'api/delegates';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

export const useCurrentTripRequestDelegates = (): {
  delegates: string | string[];
} => {
  const [delegates, setDelegates] = useState<string[] | string>('');
  // TODO заменить selfStore на реализацию через hook
  const {
    selfStore: {
      selfEmployee: { organizationId: orgId, departmentId: depId },
    },
  } = useAppStoreContext();

  const { currentTripRequest } = useCurrentTripRequest();
  const approvedBy = currentTripRequest?.approvedBy;
  const supId = approvedBy?.id;

  const { data: delegatesData } = useGetDelegates(
    {
      orgId, depId, supId,
    },
    {
      enabled: orgId && depId && supId,
      ...CLEAR_QUERY_CONFIG,
    }
  );

  useEffect(() => {
    const delegateListString = (delegatesData || []).map(delegate => {
      const {
        lastName, firstName, patronymic,
      } = delegate.delegateEmployee;
      return `${lastName} ${firstName.charAt(0)}. ${patronymic?.charAt(0)}. `;
    });
    setDelegates(delegateListString.length ? delegateListString : 'Список делегатов пуст');
  }, [delegatesData]);

  return { delegates };
};
