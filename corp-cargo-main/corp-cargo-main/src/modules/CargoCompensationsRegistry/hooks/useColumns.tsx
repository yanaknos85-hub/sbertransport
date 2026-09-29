import { useCallback, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { SortOrder } from 'antd/lib/table/interface';

import {
  useCargoCompensationUserSettings,
  useDefaultCompensationColumnVisibilitySettings,
  useSaveCargoCompensationsUsersAttributes
} from 'api/cargo-registry-compensations-search';

import { useProfile } from 'api/profile';
import { SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { sortFields, VisibleFields } from '../constants';
import { Column, ColumnVisibilitySettings } from '../types';

interface UseColumnsResult {
  columns: Column[];
  settings: {
    columnVisibility: ColumnVisibilitySettings;
    setColumnVisibility: (settings: ColumnVisibilitySettings) => void;
    isSaving: boolean;
    defaultVisibility: ColumnVisibilitySettings;
  };
}

export const useColumns = (pathname: string, sortSetting?: SortSetting): UseColumnsResult => {
  const { t } = useTranslation();
  const { userId = '' } = useProfile().data;
  const { data: defaultColumnsVisibility } = useDefaultCompensationColumnVisibilitySettings(userId);
  const { refetch: refetchUserSettings, data: userSettings } = useCargoCompensationUserSettings(userId);
  const [saveColumnVisibilitySettings, { isLoading: isSaving }] = useSaveCargoCompensationsUsersAttributes(userId);

  const defaultVisibility = useMemo(() => {
    if (defaultColumnsVisibility.cargoUIVisibility) {
      return defaultColumnsVisibility.cargoUIVisibility;
    }
    const defaults: ColumnVisibilitySettings = {};
    Object.values(VisibleFields).forEach(item => {
      defaults[item] = true;
    });
    return defaults;
  }, [defaultColumnsVisibility.cargoUIVisibility]);

  const columnVisibility = useMemo(() => userSettings.cargoUIVisibility || defaultVisibility, [
    userSettings,
    defaultVisibility,
  ]);

  const setColumnVisibility = useCallback(
    (settings: ColumnVisibilitySettings) => {
      saveColumnVisibilitySettings(settings).then(() => refetchUserSettings());
    },
    [saveColumnVisibilitySettings, refetchUserSettings]
  );

  const columns = useMemo(
    () => [
      ...Object.values(VisibleFields)
        .filter(item => columnVisibility[item])
        .map(item => {
          const column: Column = {
            dataIndex: item,
            title: t.Forms.registryCargoCompensationsSettings[item],
            sortProperty: sortFields[item],
          };
          const sorting = column.sortProperty
            ? {
              sorter: true,
              sortOrder:
                  sortSetting?.property === column.sortProperty
                    ? ((sortSetting.directionAsc ? 'ascend' : 'descend') as SortOrder)
                    : undefined,
            }
            : {};
          if (item === VisibleFields.humanReadableId) {
            column.fixed = 'left';
          }
          return { ...column, ...sorting };
        }),
    ],
    [pathname, t, sortSetting, columnVisibility]
  );

  return {
    columns,
    settings: {
      columnVisibility,
      setColumnVisibility,
      isSaving,
      defaultVisibility,
    },
  };
};
