import { useCallback } from 'react';

import { ColumnType } from 'antd/lib/table';

import { RegisterSearchQuery, UsersAttributes } from 'api/register-search';
import { TableRecord } from '../types/types';
import { TaxiRegistryColumnProps, useColumns } from './useColumns';

export const useTable = (
  setSortingSetting: (sortSetting: RegisterSearchQuery['sortSetting']) => void,
  deleteSortSettings: () => void,
  sortSetting: RegisterSearchQuery['sortSetting'],
  settingAttributes: UsersAttributes | undefined
): {
    onTableMetaChange: (_pagination: any, _: any, sorter: any) => void;
    columnsRegistryTaxi: ColumnType<TableRecord>[];
  } => {
  const onTableMetaChange = useCallback(
    (_pagination, _, sorter) => {
      if (sorter?.column?.sortProperty) {
        const sortSettings
          = Array.isArray(sorter) || !sorter.field
            ? undefined
            : {
              property: sorter.column.sortProperty,
              directionAsc: sorter.order === 'ascend',
            };
        // @ts-ignore
        setSortingSetting(sortSettings);
      } else {
        setSortingSetting(undefined);
        deleteSortSettings();
      }
    },
    [setSortingSetting, deleteSortSettings]
  );

  // Фильтрация временно отключена из за неактуальности taxiUIVisibility, до обновления api

  const columnsRegistryTaxi = [
    ...useColumns(sortSetting)
    // .map(setting => {
    //   const newSetting = { ...setting };
    //   if (settingAttributes?.taxiUIVisibility) {
    //     const keys = Object.keys(settingAttributes?.taxiUIVisibility as { [x: string]: boolean });
    //     const key = keys[keys.findIndex(k => k === setting.key)];

      //     newSetting.checked = key ? settingAttributes.taxiUIVisibility[key] : false;
      //   }
      //   return newSetting;
      // })
      // .filter(setting => setting.checked)
      .map<TaxiRegistryColumnProps>((setting, index) => ({
        ...setting,
        fixed: index === 0 ? 'left' : undefined,
      })),
  ];

  return {
    onTableMetaChange,
    columnsRegistryTaxi,
  };
};
