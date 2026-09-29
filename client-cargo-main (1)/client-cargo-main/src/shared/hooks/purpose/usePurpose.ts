import { useEffect, useState } from 'react';

import { useGetPurposeById } from 'api/purposes';
import { TripPurpose } from 'stores/Trip/Trip.interface';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { useAppStoreContext } from '../useEmpContext';

/**
 * Хук для получения цели
 */
const usePurpose = (
  purposeId: string | undefined
): {
    purpose: TripPurpose | undefined;
    inProgress: boolean;
    refetchPurpose: (options?: any) => Promise<TripPurpose>;
  } => {
  const { selfStore } = useAppStoreContext();

  const { orgId } = selfStore;

  const [purpose, _setPurpose] = useState<TripPurpose | undefined>(undefined);

  const {
    data,
    isFetching,
    isLoading,
    refetch: refetchPurpose,
  } = useGetPurposeById(
    { orgId, purposeId },
    {
      enabled: orgId !== undefined && purposeId !== undefined,
      ...CLEAR_QUERY_CONFIG,
    }
  );

  const inProgress = isFetching || isLoading;

  useEffect(() => {
    _setPurpose(data);
  }, [data]);

  return {
    purpose, inProgress, refetchPurpose,
  };
};

export default usePurpose;
