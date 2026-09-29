import React, { useMemo } from 'react';
import { Tooltip } from 'antd';
import moment from 'moment';
import type { ColumnsType } from 'antd/lib/table';
import { useTranslation } from 'i18n';

import { YandexTaxiRequestStatusTitles } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { useProfile } from 'api/profile';
import { useGetAvailableTransportTypes } from 'api/transport-types';

import { DATE_FORMAT, TripRequestTitles, VALUE_NOT_FOUND } from 'constants/constants.app';
import { TableField } from '../../fraudMonitoring.constants';
import { autoApprovalTitle, columnsWidth } from './fraudTable.constants';

import { fullName } from 'utils/employee';
import { getTripStatusTitle } from 'utils/getTripStatusTitle';
import { getTransportTypeName } from './utils';

import { RequestNumberRenderer } from './components/RequestNumberRenderer/RequestNumberRenderer';
import { ToolipRenderer } from './components/TooltipRenderer/TooltipRenderer';

import type { TReportItem } from '../../fraudMonitoring.interface';

import styles from './styles.module.scss';

export const useColumns = (): ColumnsType<TReportItem> => {
  const {
    fraudMonitoring: { tableField: i18n },
  } = useTranslation().t;
  const { organizationId } = useProfile().data;
  const allTransportTypes = useGetAvailableTransportTypes(organizationId).data;
  const getTransportTypeTitle = getTransportTypeName(allTransportTypes);

  return useMemo(
    () => [
      {
        dataIndex: TableField.humanReadableId,
        title: i18n.humanReadableId,
        fixed: 'left',
        width: columnsWidth.humanReadableId,
        render: (humarReadableId, record) => (
          <RequestNumberRenderer id={record.id}>{humarReadableId}</RequestNumberRenderer>
        ),
      },
      {
        dataIndex: TableField.transportType,
        title: i18n.transportType,
        width: columnsWidth.transportType,
        render: currentTransportType => (
          <ToolipRenderer>{getTransportTypeTitle(currentTransportType)}</ToolipRenderer>
        ),
      },
      {
        dataIndex: TableField.fraudMarkers,
        title: i18n.markers,
        width: columnsWidth.fraudMarkers,
        render: markers => markers?.length ? (
          <Tooltip
            placement="bottom"
            title={markers.map(marker => (
              <div>{marker.comment}</div>
            ))}
            overlayClassName={styles.fraudMarkersTooltip}
          >
            <div className={styles.marker}>{markers.length}</div>
          </Tooltip>
        ) : (
          VALUE_NOT_FOUND
        ),
      },
      {
        dataIndex: TableField.desiredDate,
        title: i18n.tripDate,
        width: columnsWidth.desiredDate,
        render: date => (date ? moment(date).format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : VALUE_NOT_FOUND),
      },
      {
        dataIndex: TableField.passenger,
        title: i18n.passenger,
        width: columnsWidth.passenger,
        render: passenger => <ToolipRenderer>{passenger ? fullName(passenger) : VALUE_NOT_FOUND}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.approvalDate,
        title: i18n.approvalDate,
        width: columnsWidth.approvalDate,
        render: date => (date ? moment(date).format(DATE_FORMAT.BASE_REVERTED_DOTS) : VALUE_NOT_FOUND),
      },
      {
        dataIndex: TableField.approver,
        title: i18n.approver,
        width: columnsWidth.approver,
        render: approver => <ToolipRenderer>{approver ? fullName(approver) : VALUE_NOT_FOUND}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.autoApproval,
        title: i18n.autoApproval,
        width: columnsWidth.autoApproval,
        render: flag => autoApprovalTitle[flag],
      },
      {
        dataIndex: TableField.plannedCost,
        width: columnsWidth.plannedCost,
        title: i18n.plannedCost,
        render: plannedCost => <ToolipRenderer textAlign="right">{plannedCost}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.actualCost,
        width: columnsWidth.actualCost,
        title: i18n.factCost,
        render: actualCost => <ToolipRenderer textAlign="right">{actualCost}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.departureAddress,
        title: i18n.departureAddress,
        width: columnsWidth.departureAddress,
        render: depatureAddress => <ToolipRenderer>{depatureAddress}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.intermediatePointsCount,
        title: i18n.intermediatePointsCount,
        width: columnsWidth.intermediatePointsCount,
        render: pointsCount => pointsCount || VALUE_NOT_FOUND,
      },
      {
        dataIndex: TableField.destinationAddress,
        title: i18n.destinationAddress,
        width: columnsWidth.destinationAddress,
        render: destinationAddress => <ToolipRenderer>{destinationAddress}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.status,
        title: i18n.status,
        width: columnsWidth.status,
        render: status => (
          <ToolipRenderer>
            {getTripStatusTitle(status, { ...YandexTaxiRequestStatusTitles, ...TripRequestTitles })}
          </ToolipRenderer>
        ),
      },
      {
        dataIndex: TableField.department,
        title: i18n.department,
        width: columnsWidth.department,
        render: department => <ToolipRenderer>{department}</ToolipRenderer>,
      },
      {
        dataIndex: TableField.purpose,
        title: i18n.purpose,
        width: columnsWidth.purpose,
        render: purpose => <ToolipRenderer>{purpose?.purpose ? purpose.purpose : VALUE_NOT_FOUND}</ToolipRenderer>,
      },
    ],
    []
  );
};
