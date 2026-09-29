import { TablePaginationConfig } from 'antd/lib/table';
import { Dispatch, SetStateAction, useState } from 'react';
import { useTranslation } from 'i18n';
import { UsersAttributes } from 'api/register-search';
import { DefaultValues } from 'constants/constants.app';
import {
  PublicRegistryFilters,
  PublicUIVisibilityDTO,
  SortSettings
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { applyPaginationToFilters, applySorterToFilters } from '../utils/utils';
import { TripRegistryColumnProps, TripRegistrySorterResult, useColumns } from './useColumns';

export const useTable = (
  filters: PublicRegistryFilters,
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>,
  userSortSettings: SortSettings | undefined,
  userColumnVisibilitySettings: UsersAttributes | undefined,
  isStatusChangeActive: boolean,
  applySorter: (sorter: TripRegistrySorterResult) => void
) => {
  const { t } = useTranslation();

  const [isVisibleChangeStatusModal, setVisibleChangeStatusModal] = useState<boolean>(false);
  const [tableChangeParams, setTableChangeParams] = useState<PublicRegistryFilters>(filters);

  const onTableChange = (
    pagination: TablePaginationConfig,
    _: unknown,
    sorter: TripRegistrySorterResult | TripRegistrySorterResult[]
  ) => {
    let newFilterParams = { ...filters };
    // Pagination
    const isPageChanged = pagination?.current !== filters.pageSetting.page + 1;
    const isSizeChanged = pagination.pageSize !== filters.pageSetting.size;
    if (isPageChanged || isSizeChanged) {
      newFilterParams = applyPaginationToFilters(newFilterParams, pagination);
    } else {
      // Sorting
      if (!Array.isArray(sorter)) {
        newFilterParams = applySorterToFilters(newFilterParams, sorter);
        applySorter(sorter);
      }
    }
    isStatusChangeActive && setVisibleChangeStatusModal(true);
    isStatusChangeActive ? setTableChangeParams(newFilterParams) : setFilterParams(newFilterParams);
  };

  // Фильтрация временно отключена из за неактуальности PublicUIVisibilityDTO, до обновления api

  const tableColumns = [
    ...useColumns(isStatusChangeActive, userSortSettings)
      // .map(column => {
      // if (userColumnVisibilitySettings?.publicUIVisibility) {
      //   const keys = Object.keys(userColumnVisibilitySettings?.publicUIVisibility);
      //   const key = keys.find(key => key === column.key) as keyof PublicUIVisibilityDTO;
      //   column.checked = key ? userColumnVisibilitySettings.publicUIVisibility[key] : false;
      // }
      // return column;
      // })
      // .filter(setting => setting.checked || (setting.key === 'isPaid' && isStatusChangeActive))
      .map<TripRegistryColumnProps>((setting, index) => ({
        ...setting,
        fixed: index === 0 ? 'left' : undefined,
      })),
  ];

  return {
    onTableChange,
    tableColumns,
    tableChangeParams,
    isVisibleChangeStatusModal,
    setVisibleChangeStatusModal,
  };
};
