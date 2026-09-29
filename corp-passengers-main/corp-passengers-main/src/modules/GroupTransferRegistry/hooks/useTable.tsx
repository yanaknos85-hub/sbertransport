import { useEffect, useState } from 'react';

import { useColumns } from './useColumns';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { TransportTypes } from '../types/types';
import { IUiPreferences } from 'stores/Registry/Registry.interface';

export const useTable = (
  userId: string | undefined
) => {
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const [uiPreferences, setUiPreferences] = useState<IUiPreferences>();

  useEffect(() => {
    register.getUiPreferences(userId, `registry_${TransportTypes.GROUP_TRANSFER.toLowerCase()}`).then(el => setUiPreferences(el));
  }, []);

  // Фильтрация временно отключена из за неактуальности PersonalUIVisibilityDTO, до обновления api

  const sortedColumns = [
    ...useColumns()
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

  const tableColumns = uiPreferences?.controls && uiPreferences?.controls.length > 0
    ? register.saveSettings.length > 0 ? sortedColumnsSaveSettingss : sortedColumnsUiPreferences
    : sortedColumnsSaveSettingss;

  return {
    tableColumns,
  };
};
