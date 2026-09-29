import { useFilteredTariffs as useFilteredCargoTariffs, useTariff as useTariffCargo } from 'api/tariffs-cargo';
import { useMemo } from 'react';
import { UUID } from 'utils/io-ts';

// TODO переделать any
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useTariffs = (query: any) => {
  const {
    page: pageNumber, size: pageSize, contractType, ...filters
  } = query;
  const cargoData = useFilteredCargoTariffs(
    {
      page: { pageNumber, pageSize },
      contractType,
      ...filters,
    },
    {
      enabled: true,
    }
  );
  const data = useMemo(() => cargoData, [cargoData]);
  return data;
};

export const useTariff = (tariffId: UUID, transTypeId: string, suspense?: boolean) => {
  const cargoData = useTariffCargo(
    {
      tariffId,
      transTypeId,
    },
    {
      enabled: !!tariffId,
      suspense,
    }
  );

  const data = useMemo(() => cargoData, [cargoData]);

  return data;
};
