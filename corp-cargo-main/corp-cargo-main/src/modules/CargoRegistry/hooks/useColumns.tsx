import React, { useCallback, useMemo } from 'react';
import { useTranslation } from 'i18n';
import { SortOrder } from 'antd/lib/table/interface';
import {
  useCargoUserSettings,
  useDefaultCargoColumnVisibilitySettings,
  useSaveCargoUsersAttributes
} from 'api/cargo-registry-search';
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
  const { data: defaultColumnsVisibility } = useDefaultCargoColumnVisibilitySettings();
  const { refetch: refetchUserSettings, data: userSettings } = useCargoUserSettings(userId);
  const [saveColumnVisibilitySettings, { isLoading: isSaving }] = useSaveCargoUsersAttributes(userId);

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
            title: t.Forms.registryCargoSettings[item],
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
            /* Скрыто в рамках задачи TRANSPORT 12346, ожидаем дизайн карточки
            column.render = (value: string, record: Row) => (
              <CellLink to={`${pathname}/${record.id}`}>{value}</CellLink>
            );
            */
            column.fixed = 'left';
          }
          return { ...column, ...sorting };
        }),
    ],
    /* Скрыто в рамках задачи TRANSPORT 10915, ожидаем дизайн карточки
        .concat({
          dataIndex: VisibleFields.humanReadableId,
          render: (value: string, record: Row) => (
            <Link to={`${pathname}/${record.id}`} title={t.Forms.registryFilterFields.detailedPage}>
              <EyeOutlined className={styles.eyeIcon} />
            </Link>
          ),
          fixed: 'right',
          align: 'center',
        }),
        */
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
