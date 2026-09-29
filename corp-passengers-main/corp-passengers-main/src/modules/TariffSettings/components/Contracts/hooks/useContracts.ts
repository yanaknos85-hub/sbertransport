import { useContractsById, useContractsWithParams } from 'api/contracts';
import { useProfile } from 'api/profile';
import type { UUID } from 'utils/io-ts';

// TODO переделать any
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useContracts = (query: any) => {
  const {
    page, size, ...filters
  } = query;
  const { organizationId } = useProfile().data;

  const passengersData = useContractsWithParams(
    {
      pagination: { page, size },
      organizationId,
      ...filters,
    },
    {
      enabled: true,
    }
  );

  return passengersData;
};

export const useContract = (id: UUID, suspense?: boolean) => {
  const passengersData = useContractsById(id, !!id, suspense);

  return passengersData;
};
