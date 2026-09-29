
import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import { useMemo } from 'react';

import { defaultOptions, useEmployeePersonalCars } from 'api/personalCars';

import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripTariff } from 'stores/Trip/Trip.interface';

import { useIsEditUrl } from './useIsEditUrl';

export const usePersonalCars = (
  specificEmployee?: Employee,
  tariffsInfo: ITripTariff[] = []
): {
  personalCars: PersonalCar[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  refetch?: any;
  costs?: ITripTariff[];
} => {
  // const { [StoreNames.employeeStore]: employeeStore, [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();

  const { selfEmployee } = employeeStore;
  const employee = specificEmployee ?? selfEmployee;

  const { isEditUrl } = useIsEditUrl();

  const options = isEditUrl
    ? {
      enabled: specificEmployee && specificEmployee !== selfEmployee,
      ...CLEAR_QUERY_CONFIG,
    }
    : {
      ...defaultOptions,
      cacheTime: 100, // force data caching to prevent infinite requests.
      // (Underlying useAPI hook may work unstable without caching. It returns
      // undefined before request has been completed. The request completion
      // may lead to component re-rendering which in turn calls useAPI again
      // resulting in infinite loop.)
    };

  const { data: personalCars, refetch } = useEmployeePersonalCars(employee, options);
  // const costs = tripStore.personalCarsCosts ?? []; // comment out personal car individual tariffs
  const costs = useMemo(() => {
    const personalTariff = tariffsInfo.find(item => item.transportType.name === TransportTypeEnum.PERSONAL);
    return personalTariff ? personalCars.map(() => personalTariff) : [];
  }, [tariffsInfo, personalCars]);

  return {
    personalCars,
    refetch,
    costs,
  };
};
