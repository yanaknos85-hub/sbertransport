import React, { useMemo, useState } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { Modal, Space, Tooltip } from 'antd';
import { useTranslation } from 'i18n';
import { CompensationReportsJournal, FilterRequest } from '../types';
import { CloseCircleOutlined, DownloadOutlined } from '@ant-design/icons';
import DownloadButton from 'components/DownloadButton';
import { CARGO_COMPENSATION } from 'constants/constants.api';
import { useCancelTask } from 'api/cargo-registry-compensations-search';
import { FIELD_LABELS, TaskStatuses } from '../constants';
import { DATE_FORMAT, emptySign } from 'constants/constants.app';
import moment from 'moment';
import { formatFilterRequest } from 'utils/formatFilterRequest';

export type TripRegistryColumnProps = ColumnProps<CompensationReportsJournal>;

export const useColumns = (): TripRegistryColumnProps[] => {
  const { t } = useTranslation();
  const [cancelTask, { isLoading }] = useCancelTask();
  const [taskToCancel, setTaskToCancel] = useState<string | null>(null);

  const formatApplicationFilter = formatFilterRequest<FilterRequest>(FIELD_LABELS);

  const handleCancelTask = async (taskId: string) => {
    Modal.confirm({
      title: t.Forms.registryCargoCompensationsReports.cancelModal.confirmCancelTitle,
      okText: t.Forms.registryCargoCompensationsReports.cancelModal.yes,
      cancelText: t.Forms.registryCargoCompensationsReports.cancelModal.no,
      closable: true,
      onOk: async () => {
        setTaskToCancel(taskId);
        try {
          await cancelTask(taskId);
        } finally {
          setTaskToCancel(null);
        }
      },
    });
  };

  return useMemo(
    () => [
      {
        dataIndex: 'status',
        title: <strong>{t.Forms.registryCargoCompensationsReports.tableColumns.status}</strong>,
        key: 'status',
        width: 110,
      },
      {
        dataIndex: 'url',
        title: <strong>{t.Forms.registryCargoCompensationsReports.tableColumns.url}</strong>,
        key: 'url',
        width: 300,
      },
      {
        dataIndex: 'filterRequest',
        title: <strong>{t.Forms.registryCargoCompensationsReports.tableColumns.filterRequest}</strong>,
        key: 'filterRequest',
        width: 300,
        render: (value: FilterRequest) => formatApplicationFilter(value),
      },
      {
        dataIndex: 'creationTime',
        title: <strong>{t.Forms.registryCargoCompensationsReports.tableColumns.creationTime}</strong>,
        key: 'creationTime',
        width: 200,
        render: (creationTime: number[]) => {
          if (!creationTime || !Array.isArray(creationTime) || creationTime.length < 3) return emptySign;

          const adjustedCreationTime = [...creationTime];
          adjustedCreationTime[1] = adjustedCreationTime[1] - 1;

          return moment.utc(adjustedCreationTime).format(DATE_FORMAT.BASE_REVERTED_DOTS);
        },
      },
      {
        dataIndex: 'actions',
        title: <strong>{t.Forms.registryCargoCompensationsReports.tableColumns.actions}</strong>,
        align: 'center',
        key: 'actions',
        width: 211,
        render: (_: string, record: CompensationReportsJournal) => (
          <Space size="middle">
            {(record.status === TaskStatuses.DONE) && (
              <Tooltip title={t.Forms.registryCargoCompensationsReports.tableColumns.download}>
                <span>
                  <DownloadButton
                    url={`${CARGO_COMPENSATION}${record.url}`}
                    customElement={<DownloadOutlined style={{ cursor: 'pointer' }} />}
                    skipFormat={true}
                  />
                </span>
              </Tooltip>
            )}

            {(record.status === TaskStatuses.WAIT || record.status === TaskStatuses.IN_PROGRESS) && (
              <Tooltip title={t.Forms.registryCargoCompensationsReports.tableColumns.cancel}>
                <CloseCircleOutlined
                  style={{
                    cursor: 'pointer',
                    color: taskToCancel === record.id && isLoading ? '#1890ff' : undefined,
                  }}
                  onClick={() => handleCancelTask(record.id)}
                  disabled={isLoading && taskToCancel === record.id}
                />
              </Tooltip>
            )}
          </Space>
        ),
      },
    ],
    [t, isLoading, taskToCancel]
  );
};
