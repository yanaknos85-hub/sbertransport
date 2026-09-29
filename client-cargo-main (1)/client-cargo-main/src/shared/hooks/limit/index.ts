import { Employee } from '@sber-sbertransport/mf-core';

import { useGetEmployeeLimit, useGetLimitByDepartment, useGetLimitSharing } from 'api/limits';
import { Limit, LIMIT_SERVICE_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { LimitModel } from 'stores/Limits/Models/LimitModel';
import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models';
import { UUID } from 'utils/io-ts';

import { useAppStoreContext } from '../useEmpContext';

const useLimitsByDepartment = (
  departmentId?: string
): {
  limitsByDepartment: LimitModel[];
} => {
  const { data: limitsByDepartment } = useGetLimitByDepartment(departmentId || '', { enabled: !!departmentId });

  return { limitsByDepartment };
};

const useEmployeeLimit = (employee?: Employee): Limit | undefined => {
  const now = new Date();
  const currentYear = now.getFullYear();

  const { data: employeeLimit } = useGetEmployeeLimit(employee?.id || '', currentYear, { enabled: !!employee });

  return employeeLimit;
};

const useLimitSharing = (limitId?: UUID) => {
  const { data: limitSharing } = useGetLimitSharing(limitId || ('' as UUID), { enabled: !!limitId });

  return limitSharing;
};

export const useCurrentYearDepLimit = (employee?: Employee): LimitModel[] | undefined => {
  const { limitsByDepartment } = useLimitsByDepartment(employee?.departmentId);

  const now = new Date();
  const currentYear = now.getFullYear();

  return limitsByDepartment.filter(limit => limit.year === currentYear);
};

export const useCurrentLimit = (employee?: Employee): LimitModel[] | undefined => {
  const { [StoreNames.selfStore]: selfStore, [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const currentYearDepLimit = useCurrentYearDepLimit(employee);
  const employeeIsCurrentUser = selfStore.selfEmployee.id === employee?.id;

  //  Т.к. используется для поездок, ищем лимит с типом услуги - пассажирская перевозка
  return employeeIsCurrentUser
    ? limitsStore.currentLimit?.filter(limit => limit.limitServiceType === LIMIT_SERVICE_TYPE.PASSENGER)
    : currentYearDepLimit;
};

const getCurrentDepartmentSharing = (limits: LimitModel[] | undefined) => {
  const result: any[] = [];
  if (limits) {
    limits.forEach(limit => result.push(useLimitSharing(limit.id)));
  }
  return result;
};

/**
 * Хук для получения лимитов и распределений, изолировано от limitsStore
 *
 * @param employee - пользователь для которого необходимо получить данные
 */
export const useUserLimits = (
  employee?: Employee
): {
  currentLimit: LimitModel[] | undefined;
  employeeLimit: Limit | undefined;
  currentEmployeeSharing: LimitSharing[];
  currentDepartmentSharing: LimitSharing[];
} => {
  const currentLimit: LimitModel[] | undefined = useCurrentYearDepLimit(employee);
  const employeeLimit: Limit | undefined = useEmployeeLimit(employee);
  const employeeLimitSharing = useLimitSharing(employeeLimit?.id);
  const currentEmployeeSharing: LimitSharing[] = employeeLimit ? employeeLimitSharing : [];
  const currentDepartmentSharing: LimitSharing[] = getCurrentDepartmentSharing(currentLimit);

  return {
    currentLimit, currentEmployeeSharing, currentDepartmentSharing, employeeLimit,
  };
};

export const useTripLimitHolder = (request?: TripRequestModel | null): Employee => {
  const { [StoreNames.selfStore]: selfStore } = useAppStoreContext();

  return request?.author || selfStore.selfEmployee;
};
