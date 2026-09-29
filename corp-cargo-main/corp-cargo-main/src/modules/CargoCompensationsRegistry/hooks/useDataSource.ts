import {
  Dispatch, SetStateAction, useEffect, useMemo
} from 'react';
import { useProfile } from 'api/profile';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { Row, TransformedFilterValues } from '../types';
import { useTransformedData } from './useTransformedData';
import { useSearchCargoCompensations } from 'api/cargo-registry-compensations-search';

interface UseDataSourceProps {
  filterValues?: TransformedFilterValues;
  sortSetting?: SortSetting;
  pageSetting?: PageSetting;
  setPageSetting: Dispatch<SetStateAction<PageSetting>>;
}

interface UseDataSourceResult {
  dataSource: Row[];
  isLoading: boolean;
  metadata: { total: number; page: number; pageSize: number };
  refetch: () => void;
}

export const useDataSource = ({
  filterValues,
  sortSetting,
  pageSetting,
}: UseDataSourceProps): UseDataSourceResult => {
  const { organizationId } = useProfile().data;

  const {
    data, refetch, isLoading,
  } = useSearchCargoCompensations(
    {
      ...filterValues,
      sortSetting,
      pageSetting,
    },
    { enabled: false },
    organizationId
  );

  const dataSource = useTransformedData(data?.content);

  const metadata = useMemo(
    () => ({
      total: data?.totalElements || 0,
      page: data?.pageable?.pageNumber || 0,
      pageSize: data?.pageable?.pageSize,
    }),
    [data]
  );

  useEffect(() => {
    if (!organizationId) return;
    refetch();
  }, [refetch, filterValues, organizationId, pageSetting, sortSetting]);

  return {
    dataSource,
    isLoading,
    metadata,
    refetch,
  };
};
