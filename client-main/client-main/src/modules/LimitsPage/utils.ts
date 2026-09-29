import { IEmployeeStore } from '@sber-sbertransport/mf-core';
import { LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';
import { UUID } from 'utils/io-ts';

export const isInitRequest = (status: LIMIT_REQUEST_STATUS | string): boolean => status === LIMIT_REQUEST_STATUS.INIT;

export const isRequestCancelled = (status: LIMIT_REQUEST_STATUS | string): boolean => status === LIMIT_REQUEST_STATUS.CANCELLED;

export const chooseColorByPercent = (percent: number): string => {
  if (!percent) return 'var(--gray-7)';

  switch (true) {
    case percent <= 30:
      return 'var(--red-color)';
    case percent <= 60:
      return 'var(--neon-carrot)';
    case percent > 60:
      return 'var(--font-green-color)';
    default:
      return 'var(--gray-7)';
  }
};

export const getEmpInitials = (employeeId: string | UUID, empStore: IEmployeeStore): string => {
  const data = empStore.employeeListByOrgMapped[employeeId];
  return data ? `${data.firstName} ${data.patronymic} ${data.lastName}` : '-';
};
