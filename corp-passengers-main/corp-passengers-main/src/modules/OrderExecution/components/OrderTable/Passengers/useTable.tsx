import { useEffect, useState } from 'react';
import { useTableFields } from './useTableFields';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { IUiPreferences } from 'stores/Registry/Registry.interface';
import { FilterValues } from './types';

import { OrderExecutionColumnProps } from './types';

export interface IUseTable {
  columnsOrderExecution: OrderExecutionColumnProps[];
}

export const useTable = (
  userId: string | undefined,
  filterValues: FilterValues
): IUseTable => {
  const { [StoreNames.registryStore]: register, passengerStore } = useAppStoreContext();
  const [uiPreferences, setUiPreferences] = useState<IUiPreferences>();

  useEffect(() => {
    if (passengerStore.personQueryFilters.transportType) {
      register.getUiPreferencesOto(userId, `orderExecution_${passengerStore.personQueryFilters.transportType.toLowerCase()}`).then(el => setUiPreferences(el));
    }
  }, [filterValues, passengerStore.personQueryFilters.transportType]);

  const sortedColumns = [
    ...useTableFields()
      .map(setting => {
        const newSetting = { ...setting };
        if (uiPreferences?.controls && uiPreferences?.controls.length > 0) {
          if (register.saveSettings.length < 1) {
            const key = uiPreferences.controls[uiPreferences?.controls.length - 1].settings
              .filter(el => el.valueSetting === newSetting.key);

            newSetting.checked = !!key.length;
          }
        }
        if (register.saveSettings.length > 0) {
          const key = register.saveSettings.filter(el => el[0] === newSetting.key);
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
    .sort((a, b) => sortMap[a.key as string] - sortMap[b.key as string]);

  const sortedColumnsSaveSettingss = sortedColumns.sort((a, b) => {
    const indexA = sortIndexMap[a.key!] ?? Infinity;
    const indexB = sortIndexMap[b.key!] ?? Infinity;

    return indexA - indexB;
  });

  const columnsOrderExecution = uiPreferences?.controls && uiPreferences?.controls.length > 0
    ? register.saveSettings.length > 0 ? sortedColumnsSaveSettingss : sortedColumnsUiPreferences
    : sortedColumnsSaveSettingss;

  return {
    columnsOrderExecution,
  } as IUseTable;
};
