import React, { useMemo, useState } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import { BusinessJournalRecord, FilterRequest } from '../types/types';
import { Modal, Space, Tooltip } from 'antd';
import { CloseCircleOutlined, DownloadOutlined, ReloadOutlined } from '@ant-design/icons';
import DownloadButton from 'components/DownloadButton';
import { CARGO_REPORTS } from 'constants/constants.api';
import { useCancelTask } from 'api/business-reports';
import { FIELD_LABELS, Statuses } from '../constants/constants';
import { DATE_FORMAT, emptySign } from 'constants/constants.app';
import moment from 'moment';
import { useBusinessReportsContext } from '../context/BusinessReports.context';
import { formatFilterRequest } from 'utils/formatFilterRequest';

export type TripRegistryColumnProps = ColumnProps<BusinessJournalRecord>;

export const useColumns = (): TripRegistryColumnProps[] => {
  const { t } = useTranslation();
  const [cancelTask, { isLoading }] = useCancelTask();
  const [taskToCancel, setTaskToCancel] = useState<string | null>(null);
  const { setInitialFilters } = useBusinessReportsContext();

  /**
   * @param {FilterRequest} filterRequest - Объект с параметрами фильтрации заявок
   */

  const formatApplicationFilter = formatFilterRequest<FilterRequest>(FIELD_LABELS);

  const handleCancelTask = async (taskId: string) => {
    Modal.confirm({
      title: t.Forms.businessReports.cancelModal.confirmCancelTitle,
      okText: t.Forms.businessReports.cancelModal.yes,
      cancelText: t.Forms.businessReports.cancelModal.no,
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

  const transformFilterRequestToFormValues = (filterRequest: FilterRequest): FilterRequest => {
    if (!filterRequest) return {};
    return {
      requestHumanId: filterRequest.requestHumanId,
      cargoTransportType: filterRequest.cargoTransportType,
      requestStatusSet: filterRequest.requestStatusSet,
      organizationSet: filterRequest.organizationSet,
      contractorSet: filterRequest.contractorSet,
      authorFIO: filterRequest.authorFIO,
      deadlineDate: filterRequest.deadlineDate,
      desiredDate: filterRequest.desiredDate,
      creationDate: filterRequest.creationDate,
      changeDate: filterRequest.changeDate,
      department1: filterRequest.department1,
      department2: filterRequest.department2,
      department3: filterRequest.department3,
      department4: filterRequest.department4,
      department5: filterRequest.department5,
      department6: filterRequest.department6,
    };
  };

  const handleRepeat = (filterRequest: FilterRequest) => {
    const initialValues = transformFilterRequestToFormValues(filterRequest);
    if (setInitialFilters) {
      setInitialFilters(initialValues);
    }
  };

  return useMemo(
    () => [
      {
        dataIndex: 'status',
        title: <strong>{t.Forms.businessReports.tableColumns.status}</strong>,
        key: 'status',
        width: 110,
      },
      {
        dataIndex: 'filterRequest',
        title: <strong>{t.Forms.businessReports.tableColumns.filterRequests}</strong>,
        key: 'filterRequest',
        width: 300,
        render: (value: FilterRequest) => formatApplicationFilter(value),
      },
      {
        dataIndex: 'creationTime',
        title: <strong>{t.Forms.businessReports.tableColumns.creationTime}</strong>,
        key: 'creationTime',
        width: 200,
        render: (creationTime: string) => (
          creationTime
            ? moment(creationTime).utc().format(DATE_FORMAT.BASE_REVERTED_DOTS)
            : emptySign
        ),
      },
      {
        dataIndex: 'actions',
        title: <strong>{t.Forms.businessReports.tableColumns.actions}</strong>,
        align: 'center',
        key: 'actions',
        width: 211,
        render: (_: string, record: BusinessJournalRecord) => (
          <Space size="middle">
            {(record.status === Statuses.DONE) && (
              <Tooltip title={t.Forms.businessReports.tableColumns.download}>
                <span>
                  <DownloadButton
                    url={`${CARGO_REPORTS}${record.url}`}
                    customElement={<DownloadOutlined style={{ cursor: 'pointer' }} />}
                    skipFormat={true}
                  />
                </span>
              </Tooltip>
            )}

            {(record.status === Statuses.WAIT || record.status === Statuses.IN_PROGRESS) && (
              <Tooltip title={t.Forms.businessReports.tableColumns.cancel}>
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

            <Tooltip title={t.Forms.businessReports.tableColumns.repeat}>
              <ReloadOutlined
                style={{ cursor: 'pointer' }}
                onClick={() => handleRepeat(record.filterRequest)}
              />
            </Tooltip>
          </Space>
        ),
      },
    ],
    [t, isLoading, taskToCancel]
  );
};
