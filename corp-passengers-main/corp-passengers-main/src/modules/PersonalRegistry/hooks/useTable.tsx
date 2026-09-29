import { TablePaginationConfig } from 'antd/lib/table';
import {
  Dispatch, SetStateAction, useEffect, useState
} from 'react';

import {
  PersonalRegistryFilters,
  SortSettings
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { TripRegistrySorterResult, useColumns } from './useColumns';
import { applyPaginationToFilters, applySorterToFilters } from '../utils';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { IUiPreferences } from 'stores/Registry/Registry.interface';

export const useTable = (
  filters: PersonalRegistryFilters,
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters>>,
  userSortSettings: SortSettings | undefined,
  isStatusChangeActive: boolean,
  applySorter: (sorter: TripRegistrySorterResult) => void,
  userId: string | undefined
) => {
  const [isVisibleChangeStatusModal, setVisibleChangeStatusModal] = useState(false);
  const tableChangeParams: PersonalRegistryFilters = filters;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const [uiPreferences, setUiPreferences] = useState<IUiPreferences>();

  useEffect(() => {
    register.getUiPreferences(userId, 'registry_personal').then(el => setUiPreferences(el));
  }, [filters]);

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
    setFilterParams(newFilterParams);
  };

  // Фильтрация временно отключена из за неактуальности PersonalUIVisibilityDTO, до обновления api

  const sortedColumns = [
    ...useColumns(isStatusChangeActive, userSortSettings)
      .map(setting => {
        const newSetting = { ...setting };
        if (uiPreferences?.controls && uiPreferences?.controls.length > 0) {
          if (register.saveSettings.length < 1) {
            const key = uiPreferences.controls[uiPreferences?.controls.length - 1].settings
              .filter(el => el.valueSetting === newSetting.dataIndex);

            newSetting.checked = key.length ? true : false;
          }
        }
        if (register.saveSettings.length > 0) {
          const key = register.saveSettings.filter(el => el[0] === newSetting.dataIndex);
          const value = key[0][1];
          newSetting.checked = key ? value.active : false;
        }

        return newSetting;
      })
      .filter(setting => uiPreferences?.controls && uiPreferences?.controls.length > 0
        ? register.saveSettings.length > 0 ? setting.checked : setting.checked
        : register.saveSettings.length > 0 ? setting.checked : setting
      ),
  ];

  const sortIndexMap = register.saveSettings.reduce((map, [key, { index }]) => {
    map[key] = index;
    return map;
  }, {});

  const sortMap = uiPreferences?.controls && uiPreferences?.controls.length > 0
    && Object.fromEntries(uiPreferences.controls[uiPreferences?.controls.length - 1].settings
      .map(item => [item.valueSetting, item.sort]));

  const sortedColumnsUiPreferences = sortMap && sortedColumns
    .sort((a, b) => sortMap[a.dataIndex as string] - sortMap[b.dataIndex as string]);

  const sortedColumnsSaveSettingss = sortedColumns.sort((a, b) => {
    const indexA = sortIndexMap[a.key!] ?? Infinity;
    const indexB = sortIndexMap[b.key!] ?? Infinity;

    return indexA - indexB;
  });

  const tableColumns = uiPreferences?.controls && uiPreferences?.controls.length > 0
    ? register.saveSettings.length > 0 ? sortedColumnsSaveSettingss : sortedColumnsUiPreferences
    : sortedColumnsSaveSettingss;

  return {
    onTableChange,
    tableColumns,
    tableChangeParams,
    isVisibleChangeStatusModal,
    setVisibleChangeStatusModal,
  };
};
