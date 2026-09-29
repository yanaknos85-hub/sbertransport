import React, { FC } from 'react';
import { Spin, Table, Typography } from 'antd';
import { useTranslation } from 'i18n';
import { useTableDataDeadline } from 'modules/ServiceSettings/hooks/useTableDataDeadline';
import styles from './SettingsTable.module.scss';
import { DeadlineColumns } from '../../types/types';
import { DeadlineSettings, useColumns } from '../../hooks/useColumns';

type TableData = Record<string, DeadlineSettings[]>;

export const useSettingsTable = (
  type?: string
): {
  tableData: TableData;
  subtitles: Record<string, string>;
  columns: DeadlineColumns;
} => {
  const { t } = useTranslation();
  const tableData = useTableDataDeadline(type);
  const columns = useColumns();
  const subtitles = t.DeadlineSettings.SubtitlesCategory[type === 'cargo' ? 'cargo' : 'passengers'];

  return {
    tableData,
    subtitles,
    columns,
  };
};

export const SettingsTable: FC<{ busy: boolean; type?: string }> = ({ busy, type }) => {
  const { Title } = Typography;
  const {
    tableData, subtitles, columns,
  } = useSettingsTable(type);

  return (
    <>
      {Object.keys(subtitles).map(key => (
        <div key={key} className={styles.tableWrapper}>
          <Title
            key={key}
            className={styles.title}
            level={4}
          >
            {subtitles[key as keyof typeof subtitles]}
          </Title>
          <Spin key={key} spinning={busy}>
            <Table
              key={key}
              dataSource={tableData[key as keyof DeadlineColumns]}
              className={styles.tableLayout}
              pagination={false}
              columns={columns[key as keyof DeadlineColumns]}
              size="large"
              tableLayout="auto"
            />
          </Spin>
        </div>
      ))}
    </>
  );
};
