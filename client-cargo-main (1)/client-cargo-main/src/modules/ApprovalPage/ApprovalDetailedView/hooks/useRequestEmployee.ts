import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models';

export function useRequestEmployee(request: TripRequestModel | undefined): {
  employee: EmployeeModel | undefined;
  department: string;
} {
  const { [StoreNames.corporateStore]: corpStore, [StoreNames.employeeStore]: empStore } = useAppStoreContext();
  const { getDepartment } = corpStore;
  const employee: EmployeeModel | undefined = empStore.employeeListByOrgMapped[request?.passenger.id || ''];
  const department = employee?.departmentId ? getDepartment(employee.departmentId)?.departmentName : '';

  return {
    employee,
    department,
  };
}
