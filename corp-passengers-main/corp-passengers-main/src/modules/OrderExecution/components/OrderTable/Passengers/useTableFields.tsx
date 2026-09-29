import React, { useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { Descriptions } from 'antd';

import uuid from 'utils/uuid';
import { DATE_FORMAT, DefaultValues } from 'constants/constants.app';
import { useTransportTypes } from 'api/transport-types';
import { fullNameLastFirstPat } from 'utils/employee';
import { FeedContent } from 'stores/Engineer/Models/Feed/Feed.content';
import { formatAddress, formatWaypoints } from 'utils/formatAddress';
import { serializeRubles } from 'utils';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatDistance } from 'utils/formatDistance';
import { formatTime, getTimeString } from 'utils/formatTime';
import { STATUSES } from '../../../constants/Statuses';
import { EditableField } from '../../EditableField';
import { renderAddress } from '../../../hooks/useColumns';
import { zoneTimeISODate } from 'utils/times';
import { Category } from 'modules/OrderExecution/constants/Tabs';
import { OrderExecutionColumnProps } from './types';

import * as routes from 'constants/constants.routes';

import styles from 'modules/Engineers/Engineer.module.scss';

export const useTableFields: () => OrderExecutionColumnProps[] = () => {
  const { t } = useTranslation();
  const { data: transportTypes } = useTransportTypes();
  const { category } = useParams<{ category: string }>();

  const createRedirectLink = (transportType: string, id: string) => {
    const type = category.toLowerCase() === Category.bus ? category.toLowerCase() : transportType.toLowerCase();

    return `${routes.ORDER_EXECUTION_PASSENGERS}/${type}/${id}`;
  };

  return useMemo<OrderExecutionColumnProps[]>(
    () => [
      {
        dataIndex: 'id',
        key: 'humanReadableId',
        title: t.Monitor.id,
        width: 200,
        fixed: 'left',
        render: (id: string, request) => (
          <div className={styles.outer}>
            <Link
              className={styles.link}
              to={createRedirectLink(request.transportType, id)}
            >
              {request.humanReadableId}
            </Link>
          </div>
        ),
      },
      {
        dataIndex: 'status',
        key: 'status',
        shouldCellUpdate: () => false,
        title: t.Monitor.status,
        width: 385,
        render: (status, request) => {
          const currentStatus = STATUSES.find(item => item.name === status);
          const statusName = currentStatus ? currentStatus.rusName : DefaultValues.emptyValueInTable;
          const statusValue = currentStatus ? currentStatus.name : DefaultValues.emptyValueInTable;
          return (
            <Descriptions size="small" column={1}>
              <Descriptions.Item key={uuid()}>
                <EditableField
                  key={uuid()}
                  id={request.id}
                  label=""
                  description={statusName}
                  status={statusValue}
                  transportType={request.transportType}
                  departureAddressCoordinates={{ latitude: 0, longitude: 0 }}
                  withAvailableFutureStatus
                />
              </Descriptions.Item>
            </Descriptions>
          );
        },
      },
      {
        dataIndex: 'transportType',
        key: 'transportType',
        title: t.Monitor.transportType,
        width: 200,
        render: transportType => {
          const currentTransportType = transportTypes.find(item => item.name === transportType);
          const transportTypeName = currentTransportType
            ? currentTransportType.rusName
            : DefaultValues.emptyValueInTable;
          return (
            <div className={styles.outer}>
              <span>{transportTypeName}</span>
            </div>
          );
        },
      },
      {
        dataIndex: 'passenger',
        key: 'passenger',
        title: t.Monitor.passenger,
        width: 220,
        render: passenger => (
          <div className={styles.outer}>
            <span>{fullNameLastFirstPat(passenger)}</span>
          </div>
        ),
      },
      {
        dataIndex: ['passenger', 'positionName'],
        key: 'positionName',
        title: t.Monitor.positionName,
        width: 160,
        render: positionName => (
          <div className={styles.outer}>
            <span className={styles.outerSpan}>{positionName}</span>
          </div>
        ),
      },
      {
        dataIndex: ['passenger', 'phone'],
        key: 'phone',
        title: t.Monitor.passengerPhoneNumber,
        width: 180,
        render: phone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(phone)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'commentForDriver',
        key: 'commentForDriver',
        title: t.Monitor.commentForDriver,
        width: 180,
        render: commentForDriver => commentForDriver ? (
          <div className={styles.outer}>
            <span className={styles.outerSpan}>{commentForDriver}</span>
          </div>
        ) : (
          DefaultValues.emptyValueInTable
        ),
      },
      {
        dataIndex: 'addresses',
        key: 'departureAddress',
        title: t.Monitor.departureAddress,
        width: 260,
        render: cellValue => renderAddress(formatAddress(cellValue[0])),
      },
      {
        dataIndex: 'addresses',
        key: 'waypointAddress',
        title: t.Monitor.waypointAddress,
        width: 260,
        render: cellValue => renderAddress(formatWaypoints(cellValue)),
      },
      {
        dataIndex: 'addresses',
        key: 'destinationAddress',
        title: t.Monitor.destinationAddress,
        width: 260,
        render: cellValue => renderAddress(formatAddress(cellValue[cellValue.length - 1])),
      },
      {
        dataIndex: 'creationTime',
        key: 'creationTime',
        title: t.Monitor.creationTime,
        width: 200,
        sorter: true,
        render: creationTime => (
          <div className={styles.outer}>
            <span>{formatTime(creationTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'desiredDate',
        key: 'desiredDate',
        title: t.Monitor.desiredDate,
        width: 200,
        render: desiredDate => (
          <div className={styles.outer}>
            <span>{formatTime(desiredDate)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'deadline',
        key: 'deadline',
        title: t.Monitor.deadline,
        width: 210,
        sorter: true,
        render: (deadLine, value) => (
          <div className={styles.outer}>
            <span>
              {
              zoneTimeISODate(deadLine, DATE_FORMAT.DATE_WITH_TIME_DOTS, value.timeZone)
              }
            </span>
          </div>
        ),
      },
      {
        dataIndex: ['expected', 'distance'],
        key: 'expectedDistance',
        title: t.Monitor.expectedDistance,
        width: 170,
        sorter: true,
        render: distance => (
          <div className={styles.outer}>
            <span>{formatDistance(distance)}</span>
          </div>
        ),
      },
      {
        dataIndex: ['expected', 'cost'],
        key: 'expectedCost',
        title: t.Monitor.expectedCost,
        width: 170,
        sorter: true,
        render: cost => (
          <div className={styles.outer}>
            <span>{serializeRubles(cost)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'waitTime',
        key: 'waitTime',
        title: t.Monitor.waitTime,
        width: 190,
        render: waitTime => (
          <div className={styles.outer}>
            <span>{getTimeString(waitTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'passengerCount',
        key: 'passengerCount',
        title: t.Monitor.passengerCount,
        width: 114,
        render: passengerCount => passengerCount ? (
          <div className={styles.outer}>
            <span>{passengerCount}</span>
          </div>
        ) : (
          DefaultValues.emptyValueInTable
        ),
      },
      {
        dataIndex: 'tariffId',
        key: 'tariffId',
        title: t.Monitor.tariffId,
        width: 100,
        render: (id: string, request: FeedContent) => (
          <div className={styles.outer}>
            <span>{request.tariffHumanReadableId}</span>
          </div>
        ),
      },
      {
        dataIndex: 'taxiClass',
        key: 'taxiClass',
        title: t.Monitor.taxiClass,
        width: 150,
        render: taxiClass => {
          const taxiClasses: Record<string, string> = t.TaxiClasses;
          return taxiClass ? (
            <div className={styles.outer}>
              <span>{taxiClasses[taxiClass]}</span>
            </div>
          ) : (
            DefaultValues.emptyValueInTable
          );
        },
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, transportTypes, category]
  );
};
