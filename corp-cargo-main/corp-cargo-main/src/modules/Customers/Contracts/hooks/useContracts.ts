/* eslint-disable no-unused-vars, @typescript-eslint/no-unused-vars */
import { useMemo } from 'react';
import { useCargoContractsWithParams, useContractsByIdCargo } from 'api/contracts';
import { useProfile } from 'api/profile';
import type { UUID } from 'utils/io-ts';
import { ContractsSearchQuery } from 'stores/Contracts/Contracts.interface';
import { processSearchContractsQuery } from '../utils/searchQuery';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useContracts = (query: any) => {
  const {
    page, size, ...filters
  } = query;
  // const { organizationId } = useProfile().data;

  const cargoData = useCargoContractsWithParams(
    {
      ...processSearchContractsQuery<ContractsSearchQuery>(filters),
      pagination: { page, size },
      // organizationId,
      // ...filters,
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
