
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

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const useTripLimit = ({ request }: { request?: TripRequestModel | null }): TripLimit => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  // const limitHolder: Employee = useTripLimitHolder(request);
  // const userLimit = useUserLimits(limitHolder);
  // const isCurrentUser = selfStore.selfEmployee.id === limitHolder.id;

  // Убрали выполнение по задаче TRANSPORT-3387 в рамках TRANSPORT-10116
  // const limitSource = isCurrentUser ? limitsStore : userLimit;
  const {
    currentLimit: currentYearDepLimit,
    employeeLimit,
    currentEmployeeSharing,
    currentDepartmentSharing,
  } = limitsStore;

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
