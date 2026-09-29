import { useCargoContractsWithParams, useContractsByIdCargo } from 'api/contracts';
import { useProfile } from 'api/profile';
import type { UUID } from 'utils/io-ts';

// TODO переделать any
export const useContracts = (query: any) => {
  const {
    page, size, ...filters
  } = query;
  const { organizationId } = useProfile().data;

  const cargoData = useCargoContractsWithParams(
    {
      pagination: { page, size },
      organizationId,
      ...filters,
    },
    {
      enabled: true,
    }
  );

  return cargoData;
};

export const useContract = (id: UUID, suspense?: boolean) => {
  const cargoData = useContractsByIdCargo(id, !!id, suspense);

  return cargoData;
};
