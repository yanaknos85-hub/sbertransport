import { useEffect, useState } from 'react';
import moment from 'moment';

import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { useGetDelegates } from 'api/delegates';
import { useDepartment } from 'api/departments';

import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

export const useCurrentTripRequestApprovers = (): {
  delegates: Delegate[];
  supervisor: IDepartmentHead | undefined;
} => {
  const [delegates, setDelegates] = useState<Delegate[]>([]);

  const { currentTripRequest } = useCurrentTripRequest();
  const organizationId = currentTripRequest?.passenger.organizationId || '';
  const departmentId = currentTripRequest?.passenger.departmentId || '';

  const { data: department } = useDepartment(organizationId, departmentId);
  const supervisorId = department?.departmentHead?.id;
  const supervisor = department?.departmentHead;

  const { data: delegatesData } = useGetDelegates(
    {
      orgId: organizationId,
      depId: departmentId,
      supId: supervisorId,
      size: 100,
    },
    {
      enabled: organizationId && departmentId && supervisorId,
      ...CLEAR_QUERY_CONFIG,
      cacheTime: 1000,
    }
  );

  useEffect(() => {
    if (delegatesData && currentTripRequest) {
      const filteredDelegates = delegatesData.content
        .filter(({
          transportType, startDate, endDate,
        }) => (
          transportType === currentTripRequest?.transportType)
          && (moment(startDate).valueOf() <= moment(currentTripRequest.creationTime).startOf('day').valueOf())
          && (moment(endDate).valueOf() >= moment(currentTripRequest.creationTime).startOf('day').valueOf())
        );
      setDelegates(filteredDelegates);
    }
  }, [delegatesData]);

  return {
    delegates,
    supervisor,
  };
};
