import { useCallback, useEffect, useState } from 'react';

import { RegisterSearchQuery } from 'api/register-search';
import { useColumns } from './useColumns';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { IUiPreferences } from 'stores/Registry/Registry.interface';

export const useTable = (
  setSortingSetting: (sortSetting: RegisterSearchQuery['sortSetting']) => void,
  deleteSortSettings: () => void,
  sortSetting: RegisterSearchQuery['sortSetting'],
  userId: string | undefined
) => {
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const [uiPreferences, setUiPreferences] = useState<IUiPreferences>();

  useEffect(() => {
    register.getUiPreferences(userId, 'registry_taxi').then(el => setUiPreferences(el));
  }, []);

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
  const sortedColumns = [
    ...useColumns(sortSetting)
      .map(setting => {
        const newSetting = { ...setting };
        if (uiPreferences?.controls && uiPreferences?.controls.length > 0) {
          if (register.saveSettings.length < 1) {
            const key = uiPreferences.controls[uiPreferences?.controls.length - 1].settings
              .filter(el => el.valueSetting === newSetting.dataIndex);

            newSetting.checked = !!key.length;
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

  const columnsRegistryTaxi = uiPreferences?.controls && uiPreferences?.controls.length > 0
    ? register.saveSettings.length > 0 ? sortedColumnsSaveSettingss : sortedColumnsUiPreferences
    : sortedColumnsSaveSettingss;

  return {
    onTableMetaChange,
    columnsRegistryTaxi,
  };
};
