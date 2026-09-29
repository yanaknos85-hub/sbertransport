import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { defaultOptions, useEmployeePersonalCars } from 'api/personalCars';
import { StoreNames } from 'stores/StoreNames.enum';
import { ITripTariff } from 'stores/Trip/Trip.interface';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { useIsEditUrl } from './useIsEditUrl';

export const usePersonalCars = (
  specificEmployee?: Employee
): {
  personalCars: PersonalCar[];
  refetch?: any;
  costs?: ITripTariff[];
} => {
  const { [StoreNames.employeeStore]: employeeStore, [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  const { selfEmployee } = employeeStore;
  const employee = specificEmployee ?? selfEmployee;

  const { isEditUrl } = useIsEditUrl();

  const options = isEditUrl
    ? {
      enabled: specificEmployee && specificEmployee !== selfEmployee,
      ...CLEAR_QUERY_CONFIG,
    }
    : defaultOptions;

  const { data: personalCars, refetch } = useEmployeePersonalCars(employee, options);
  const costs = tripStore.personalCarsCosts ?? [];

  return {
    personalCars,
    refetch,
    costs,
  };
};
