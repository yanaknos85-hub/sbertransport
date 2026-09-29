import { useState, useLayoutEffect } from 'react';
import { ColumnType } from 'antd/lib/table';
import { useAppStore } from 'ioc';
import { ignore } from 'utils/utils';

export type TableSettingsType = Record< string, { active: boolean; index: number }>;

export type SettingsColumnsType = Record<string, string>;

export const getDefaultTableSettings = (columns: SettingsColumnsType) => Object.entries(columns)
  .reduce(
    (acc, [key, _], idx) => ({
      ...acc,
      [key]: {
        active: true,
        index: idx,
      },
    }),
    {}
  ) as TableSettingsType;

/**
 * Не забудь добавить поле key столбцам, без него новые столбцы не отобразятся.
 * key гарантирует уникальность
*/
export const useTableSettings = (columns: SettingsColumnsType, url: string) => {
  const [tableSettings, setTableSettings] = useState<TableSettingsType>(getDefaultTableSettings(columns));
  const { http, logger } = useAppStore();

  useLayoutEffect(() => {
    http
      .get<TableSettingsType>(url)
      .then(({ data }) => {
        if (data && Object.keys(data).length) {
          const settings = { ...tableSettings, ...data };
          Object.keys(settings).forEach(key => {
            if (!tableSettings[key]) {
              delete settings[key];
            }
          });
          setTableSettings(settings);
        }
      })
      .catch(ignore);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [url]);

  const onSaveTableSettings = (settings: TableSettingsType) => http
    .post<TableSettingsType>(url, settings)
    .then(() => {
      setTableSettings(settings);
    })
    .catch(error => {
      logger.toMessage('error', 'Не удалось сохранить настройки');
      throw new Error(error);
    });

  const getOptimizeColumns = <T>(columns: ColumnType<T>[]) => columns
    .filter(column => tableSettings[column.key!]?.active)
    .sort((columnA, columnB) => tableSettings[columnA.key!].index - tableSettings[columnB.key!].index);

  return {
    tableSettings,
    onSaveTableSettings,
    getOptimizeColumns,
  };
};
