import {
  Dispatch, SetStateAction, useEffect, useMemo
} from 'react';
import { PageSetting, SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { Row, TransformedFilterValues } from '../types';
import { useTransformedData } from './useTransformedData';
import { useSearchCargoRegistry } from 'api/cargo-registry-route-search';
import { useOrganizationContext } from 'context/Organization.context';
import { EXECUTOR_GROUP_ALL_ID } from 'constants/constants.app';

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
  const {
    organizationId,
    executorGroupId,
    isOrganization,
    emptyExecutorGroup,
  } = useOrganizationContext();

  const orgQuery = isOrganization
    ? { organizationId }
    : {
      executorGroupIds: (executorGroupId ?? []).filter(id => id !== EXECUTOR_GROUP_ALL_ID),

      emptyExecutorGroup,
    };

  const {
    data, isLoading,
  } = useSearchCargoRegistry({
    ...filterValues, ...orgQuery, sortSetting, pageSetting,
  }, {});

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

  return {
    dataSource,
    isLoading,
    metadata,
  };
};
