import { useContractsById } from 'api/contracts';
import type { UUID } from 'utils/io-ts';

export const useContract = (id: UUID, suspense?: boolean) => {
  const passengersData = useContractsById(id, !!id, suspense);

  return passengersData;
};
