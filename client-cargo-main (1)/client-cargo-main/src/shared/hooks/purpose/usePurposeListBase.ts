import { useEffect, useState } from 'react';
import { QueryConfig } from 'react-query';

import { APIQueryResult } from 'api';
import { TripPurpose } from 'stores/Trip/Trip.interface';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

import { useAppStoreContext } from '../useEmpContext';

export interface PurposeListBase {
  purposes: TripPurpose[];
  getPurposeFromList: (purposeId: string | undefined) => TripPurpose | undefined;
  inProgress: boolean;
}

/**
 * Хук для получения списка целей
 */
const usePurposeListBase = (
  fetchMethod: (orgId: string, options: QueryConfig<TripPurpose[], unknown>) => APIQueryResult<TripPurpose[], unknown>
): PurposeListBase => {
  // TODO make with analog selfStore
  const { selfStore } = useAppStoreContext();

  const { orgId } = selfStore;

  const [purposes, _setPurposes] = useState<TripPurpose[]>([]);

  const {
    data, isLoading, isFetching,
  } = fetchMethod(orgId, {
    enabled: orgId !== undefined,
    ...CLEAR_QUERY_CONFIG,
  });

  const getPurposeFromList = (purposeId: string | undefined): TripPurpose | undefined => (data || []).find(({ id }) => purposeId === id);

  useEffect(() => {
    _setPurposes(data || []);
  }, [data]);

  const inProgress = isLoading || isFetching;

  return {
    purposes, getPurposeFromList, inProgress,
  };
};

export default usePurposeListBase;
