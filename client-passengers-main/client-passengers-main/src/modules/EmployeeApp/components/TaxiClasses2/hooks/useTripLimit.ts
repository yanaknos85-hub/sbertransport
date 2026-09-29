import { Employee } from '@sber-sbertransport/mf-core';
import { useTripLimitHolder, useUserLimits } from 'shared/hooks/limit';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { LIMIT_SERVICE_TYPE, Limit, LimitSharing } from 'stores/Limits/Limit.interface';
import { LimitModel } from 'stores/Limits/Models/LimitModel';
import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models';

export interface TripLimit {
  employeeLimit: Limit | LimitModel | undefined;
  departmentLimit: Limit | LimitModel | undefined;
  employeeSharing: LimitSharing[];
  departmentSharing: LimitSharing[];
}

export const useTripLimit = ({ request }: { request?: TripRequestModel | null }): TripLimit => {
  const { [StoreNames.selfStore]: selfStore, [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const limitHolder: Employee = useTripLimitHolder(request);
  const userLimit = useUserLimits(limitHolder);
  const isCurrentUser = selfStore.selfEmployee.id === limitHolder.id;

  const limitSource = isCurrentUser ? limitsStore : userLimit;
  const {
    currentLimit: currentYearDepLimit,
    employeeLimit,
    currentEmployeeSharing,
    currentDepartmentSharing,
  } = limitSource;

  const departmentLimit = currentYearDepLimit?.find(
    currentLimit => currentLimit.limitServiceType === LIMIT_SERVICE_TYPE.PASSENGER
  );

  return {
    employeeLimit,
    departmentLimit,
    employeeSharing: currentEmployeeSharing,
    departmentSharing: currentDepartmentSharing,
  };
};
