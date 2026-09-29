import { useEffect, useState } from 'react';
import { EmployeeModel, OrUndefined } from '@sber-sbertransport/mf-core';

interface Props<T> {
  personalCars: OrUndefined<T[]>;
  personalCarId?: string;
}

interface UseSpecificCar<T> {
  specificCar: OrUndefined<T>;
  updateWithOsago: (employee: EmployeeModel) => UpdateWithOsago<T>;
}

export type OsagoHandler<T> = (employee: EmployeeModel) => OrUndefined<T>;
export type UpdateWithOsago<T> = (osagoCar: OsagoHandler<T>) => void;

export const useSpecificCar = <T>({ personalCars, personalCarId }: Props<T>): UseSpecificCar<T> => {
  const [specificCar, setSpecificCar] = useState<OrUndefined<T>>(undefined);

  const updateWithOsago
    = (employee: EmployeeModel): UpdateWithOsago<T> => (osagoCar: OsagoHandler<T>): void => setSpecificCar(osagoCar(employee));

  useEffect(
    (): void => setSpecificCar(
      // @ts-ignore
      personalCars?.find(<T extends { id?: string }>(personalCar: T) => personalCar.id === personalCarId)
    ),
    [personalCars, personalCarId]
  );

  return {
    specificCar,
    updateWithOsago,
  };
};
