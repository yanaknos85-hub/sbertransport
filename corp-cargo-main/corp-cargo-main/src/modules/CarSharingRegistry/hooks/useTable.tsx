import { useCallback } from 'react';

import { ColumnsType, TablePaginationConfig } from 'antd/lib/table';

import { CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { useColumns } from './useColumns';
import { CarSharingRegistryColumnProps, Row, TableRecord } from '../types';
import { Key, SorterResult } from 'antd/lib/table/interface';

export const useTable = (
  setSortingSetting: (sortSetting: CarSharingSearchQuery['sortSetting']) => void,
  deleteSortSettings: () => void,
  sortSetting: CarSharingSearchQuery['sortSetting']
): {
    tableColumns: ColumnsType<TableRecord> | undefined;
    onTableMetaChange: <RecordType extends Row>(
    _pagination: TablePaginationConfig,
    _filters: Record<string, (Key | boolean)[] | null>,
    sorter: SorterResult<RecordType> | SorterResult<RecordType>[]
    ) => void;
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

        setSortingSetting(sortSettings);
      } else {
        setSortingSetting(undefined);
        deleteSortSettings();
      }
    },
    [setSortingSetting, deleteSortSettings]
  );

  const tableColumns = [
    ...useColumns(sortSetting).map<CarSharingRegistryColumnProps>((setting, index) => ({
      ...setting,
      fixed: index === 0 ? 'left' : undefined,
    })),
  ];

  return {
    tableColumns,
    onTableMetaChange,
  };
};
