import { useFilteredTariffs, useTariff as useTariffPassenger } from 'api/tariffs';
import { useMemo } from 'react';
import { UUID } from 'utils/io-ts';

// переделать any
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useTariffs = (query: any) => {
  const {
    page: pageNumber, size: pageSize, ...filters
  } = query;

  const passengersData = useFilteredTariffs(
    {
      page: { pageNumber, pageSize },
      ...filters,
    },
    {
      enabled: true,
    }
  );

  const data = useMemo(() => passengersData, [passengersData]);

  return data;
};

export const useTariff = (tariffId: UUID, transTypeId: string, suspense?: boolean) => {
  const passengersData = useTariffPassenger(
    {
      tariffId,
      transTypeId,
    },
    {
      enabled: !!tariffId,
      suspense,
    }
  );

  const data = useMemo(() => passengersData, [passengersData]);

  return data;
};
