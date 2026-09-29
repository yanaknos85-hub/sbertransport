import { TablePaginationConfig } from 'antd/lib/table';
import React, { Dispatch, SetStateAction, useState } from 'react';
import { useTranslation } from 'i18n';
import { UsersAttributes } from 'api/register-search';
import {
  PersonalRegistryFilters,
  PersonalUIVisibilityDTO,
  SortSettings
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { DefaultValues } from 'constants/constants.app';
import { TripRegistryColumnProps, TripRegistrySorterResult, useColumns } from './useColumns';
import { applyPaginationToFilters, applySorterToFilters } from '../utils';

export const useTable = (
  filters: PersonalRegistryFilters,
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters | undefined>>,
  userSortSettings: SortSettings | undefined,
  userColumnVisibilitySettings: UsersAttributes | undefined,
  isStatusChangeActive: boolean,
  applySorter: (sorter: TripRegistrySorterResult) => void
) => {
  const { t } = useTranslation();

  const [isVisibleChangeStatusModal, setVisibleChangeStatusModal] = useState(false);
  const [tableChangeParams, setTableChangeParams] = useState<PersonalRegistryFilters>(filters);

  const onTableChange = (
    pagination: TablePaginationConfig,
    _: unknown,
    sorter: TripRegistrySorterResult | TripRegistrySorterResult[]
  ) => {
    let newFilterParams = { ...filters };
    // Pagination

    const isPageChanged = pagination?.current !== filters.pageSetting?.page + 1;
    const isSizeChanged = pagination.pageSize !== filters.pageSetting?.size;
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

  // Фильтрация временно отключена из за неактуальности PersonalUIVisibilityDTO, до обновления api

  const tableColumns = [
    ...useColumns(isStatusChangeActive, userSortSettings)
      // .map(column => {
      //   if (userColumnVisibilitySettings?.personalUIVisibility) {
      //     const keys = Object.keys(userColumnVisibilitySettings?.personalUIVisibility);
      //     const key = keys.find(key => key === column.key) as keyof PersonalUIVisibilityDTO;
      //     column.checked = key ? userColumnVisibilitySettings.personalUIVisibility[key] : false;
      //   }
      //   return column;
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
