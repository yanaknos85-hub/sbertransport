import { IEmployeeStore } from '@sber-sbertransport/mf-core';
import { ILimitsStore, LimitRequestInfo } from 'stores/Limits/Limit.interface';

import { UUID } from 'utils/io-ts';

export const getLimitHumanReadableId = (sourceLimit: string, limitsStore: ILimitsStore): string | undefined => {
  const depLimit = limitsStore.departmentLimits.find(y => y.id === sourceLimit)?.humanReadableId;
  const empLimit = limitsStore.employeeLimits.find(y => y.id === sourceLimit)?.humanReadableId;
  return depLimit ?? empLimit;
};

export const getSum = (request: LimitRequestInfo): number => request.sum - request.approverDtoList.reduce((x, total) => total.sum + x, 0);

// встречается ещё в нескольких местах в системе
// возвращает инициалы пользователя
export const getEmpInitials = (employeeId: string | UUID, empStore: IEmployeeStore): string => {
  const data = empStore.employeeListByOrgMapped[employeeId];
  return data ? `${data.firstName} ${data.patronymic} ${data.lastName}` : '-';
};
