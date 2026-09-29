import {
  Dispatch, SetStateAction, useEffect, useMemo
} from 'react';
import { useProfile } from 'api/profile';
import { useSearchCargoRegistry } from 'api/cargo-registry-search';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { Row, TransformedFilterValues } from '../types';
import { useTransformedData } from './useTransformedData';

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
}

export const useDataSource = ({
  filterValues,
  sortSetting,
  pageSetting,
  setPageSetting,
}: UseDataSourceProps): UseDataSourceResult => {
  const { organizationId } = useProfile().data;
  const {
    data, refetch, isLoading,
  } = useSearchCargoRegistry(
    {
      ...filterValues, sortSetting, pageSetting,
    },
    { enabled: false },
    organizationId
  );

  const dataSource = useTransformedData(data?.content);

  const metadata = useMemo(
    () => ({
      total: data?.totalElements,
      page: data?.pageable.pageNumber,
      pageSize: data?.pageable.pageSize,
    }),
    [data]
  );

  useEffect(() => {
    setPageSetting(setting => ({ page: 0, size: setting.size }));
  }, [setPageSetting, filterValues]);

  useEffect(() => {
    refetch();
  }, [refetch, filterValues, organizationId, pageSetting, sortSetting]);

  return {
    dataSource,
    isLoading,
    metadata,
  };
};
