import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { DATE_FORMAT } from 'constants/constants.app';
import { fullNameLastFirstPat, fullNameWithPhoneNumber, fullVehicleInfo } from 'utils/employee';
import { Contractor } from 'stores/Engineer/Models/Feed/Feed.contractor';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { useTransportTypes } from 'api/transport-types';
import { formatAddress, formatWaypoints } from 'utils/formatAddress';
import { Driver } from 'stores/Engineer/Models/Feed/Feed.driver';
import { Vehicle } from 'stores/Engineer/Models/Feed/Feed.vehicle';
import { serializeRubles } from 'utils';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatDistance } from 'utils/formatDistance';
import { formatTime, getTimeString } from 'utils/formatTime';
import { FeedContent } from 'stores/Engineer/Models/Feed/Feed.content';
import { FeedCargoContentType } from 'stores/Engineer/Models/FeedCargo/Feed.content';
import { useContractors } from 'api/contractors';
import { formatDriverRating } from 'utils/formatDriverRating';
import { DatePicker, Descriptions } from 'antd';
import uuid from 'utils/uuid';
import { useChangeCargoShipmentTime, useChangeCargoTransferTime } from 'api/engineer';
import moment, { Moment } from 'moment';
import styles from 'modules/Engineers/Engineer.module.scss';
import { ChangedFieldsForm } from '../../TripDetailed/components/ChangedFields/ChangedFields';

import * as routes from 'constants/constants.routes';

export const renderAddress = (addressString: string): JSX.Element => (
  <div className={styles.outer}>
    <span>{addressString}</span>
  </div>
);

export const useRequestParamsColumns: () => ColumnProps<FeedContent>[] = () => {
  const { t } = useTranslation();
  const { data: statuses } = useGettingAllTravelStatuses();
  const { data: transportTypes } = useTransportTypes();

  return useMemo<ColumnProps<FeedContent>[]>(
    () => [
      {
        dataIndex: 'id',
        key: 'humanReadableId',
        title: t.Monitor.id,
        align: 'center',
        width: 150,
        fixed: 'left',
        sorter: true,
        render: (id: string, request) => (
          <div className={styles.outer}>
            <Link className={styles.link} to={`${routes.ORDER_EXECUTION_PASSENGERS}/${id}`}>
              {request.humanReadableId}
            </Link>
          </div>
        ),
      },
      {
        dataIndex: 'status',
        key: 'status',
        title: t.Monitor.status,
        align: 'center',
        width: 135,
        render: status => {
          const currentStatus = statuses.find(item => item.name === status);
          const statusName = currentStatus ? currentStatus.rusName : '-';
          return (
            <div className={styles.outer}>
              <span>{statusName}</span>
            </div>
          );
        },
      },
      {
        dataIndex: 'transportType',
        key: 'transportType',
        title: t.Monitor.transportType,
        align: 'center',
        width: 135,
        render: transportType => {
          const currentTransportType = transportTypes.find(item => item.name === transportType);
          const transportTypeName = currentTransportType ? currentTransportType.rusName : '-';
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
        align: 'center',
        width: 200,
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
        align: 'center',
        width: 155,
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
        align: 'center',
        width: 175,
        render: phone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(phone)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'addresses',
        key: 'departureAddress',
        title: t.Monitor.departureAddress,
        align: 'center',
        width: 200,
        render: cellValue => renderAddress(formatAddress(cellValue[0])),
      },
      {
        dataIndex: 'addresses',
        key: 'waypointAddress',
        title: t.Monitor.waypointAddress,
        align: 'center',
        width: 200,
        render: cellValue => renderAddress(formatWaypoints(cellValue)),
      },
      {
        dataIndex: 'addresses',
        key: 'destinationAddress',
        title: t.Monitor.destinationAddress,
        align: 'center',
        width: 200,
        render: cellValue => renderAddress(formatAddress(cellValue[cellValue.length - 1])),
      },
      {
        dataIndex: 'creationTime',
        key: 'creationTime',
        title: t.Monitor.creationTime,
        align: 'center',
        width: 140,
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
        align: 'center',
        width: 170,
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
        align: 'center',
        width: 150,
        sorter: true,
        render: deadLine => (
          <div className={styles.outer}>
            <span>{formatTime(deadLine)}</span>
          </div>
        ),
      },
      {
        dataIndex: ['expected', 'distance'],
        key: 'expectedDistance',
        title: t.Monitor.expectedDistance,
        align: 'center',
        width: 160,
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
        align: 'center',
        width: 160,
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
        align: 'center',
        width: 160,
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
        align: 'center',
        width: 150,
        render: passengerCount => passengerCount ? (
          <div className={styles.outer}>
            <span>{passengerCount}</span>
          </div>
        ) : (
          '-'
        ),
      },
      {
        dataIndex: 'tariffId',
        key: 'tariffId',
        title: t.Monitor.tariffId,
        align: 'center',
        width: 150,
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
        align: 'center',
        width: 120,
        render: taxiClass => {
          const taxiClasses: Record<string, string> = t.TaxiClasses;
          return taxiClass ? (
            <div className={styles.outer}>
              <span>{taxiClasses[taxiClass]}</span>
            </div>
          ) : (
            '-'
          );
        },
      },
      {
        dataIndex: 'commentForDriver',
        key: 'commentForDriver',
        title: t.Monitor.commentForDriver,
        align: 'center',
        width: 200,
        render: commentForDriver => commentForDriver ? (
          <div className={styles.outer}>
            <span className={styles.outerSpan}>{commentForDriver}</span>
          </div>
        ) : (
          '-'
        ),
      },
    ],
    [t, statuses, transportTypes]
  );
};
export const useTripParamsColumns: () => ColumnProps<FeedContent>[] = () => {
  const { t } = useTranslation();
  const { contractors } = useContractors().data;

  return useMemo<ColumnProps<FeedContent>[]>(
    () => [
      {
        dataIndex: 'contractor',
        title: t.Monitor.executor,
        align: 'center',
        width: 150,
        render: (contractor: Contractor) => {
          const currentContractor = contractor && contractors.find(item => item.id === contractor.id);
          const contractorName = currentContractor ? currentContractor.name : '-';
          return (
            <div className={styles.outer}>
              <span className={styles.outerSpan}>{contractorName}</span>
            </div>
          );
        },
      },
      {
        dataIndex: 'dateTimeRegistered',
        title: t.Monitor.dateTimeRegistered,
        align: 'center',
        width: 155,
        render: dateTimeRegistered => (
          <div className={styles.outer}>
            <span>{formatTime(dateTimeRegistered)}</span>
          </div>
        ),
      },
      {
        dataIndex: ['fact', 'factSearchTime'],
        title: t.Monitor.factSearchTime,
        align: 'center',
        width: 150,
        render: factSearchTime => (
          <div className={styles.outer}>
            <span>{getTimeString(factSearchTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: ['fact', 'tripStartTime'],
        title: t.Monitor.tripStartTime,
        align: 'center',
        width: 125,
        render: tripStartTime => (
          <div className={styles.outer}>
            <span>{formatTime(tripStartTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'tripFinishTime',
        title: t.Monitor.tripFinishTime,
        align: 'center',
        width: 165,
        render: tripFinishTime => (
          <div className={styles.outer}>
            <span>{formatTime(tripFinishTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'driver',
        title: t.Monitor.driver,
        align: 'center',
        width: 200,
        render: (driver: Driver) => driver ? (
          <div className={styles.outer}>
            <span>{fullNameWithPhoneNumber({ ...driver })}</span>
          </div>
        ) : (
          '-'
        ),
      },
      {
        dataIndex: ['driver', 'rating'],
        title: t.Monitor.rating,
        align: 'center',
        width: 120,
        render: rating => (
          <div className={styles.outer}>
            <span>{formatDriverRating(rating)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'vehicle',
        title: t.Monitor.vehicle,
        align: 'center',
        width: 190,
        render: (vehicle: Vehicle) => vehicle ? (
          <div className={styles.outer}>
            <span>{fullVehicleInfo({ ...vehicle })}</span>
          </div>
        ) : (
          '-'
        ),
      },
      {
        dataIndex: ['department', 'departmentName'],
        title: t.Monitor.department,
        align: 'center',
        width: 150,
        render: department => department ? (
          <div className={styles.outer}>
            <span>{department}</span>
          </div>
        ) : (
          '-'
        ),
      },
    ],
    [t, contractors]
  );
};
export const useCargoRequestParamsColumns: () => ColumnProps<FeedCargoContentType>[] = () => {
  const { t } = useTranslation();
  const { data: statuses } = useGettingAllTravelStatuses();
  const [changeCargoTransferTime] = useChangeCargoTransferTime();
  const [changeCargoShipmentTime] = useChangeCargoShipmentTime();
  const handleTransferTimeChanged = (value: Moment | null, request: FeedCargoContentType) => {
    let time;
    if (value === null) {
      time = '';
    } else {
      time = value.toISOString();
    }
    return changeCargoTransferTime({
      requestId: request.id,
      transferTime: time,
      status: request.status,
    });
  };

  const handleShipmentTimeChanged = (value: Moment | null, request: FeedCargoContentType) => {
    let time;
    if (value === null) {
      time = '';
    } else {
      time = value.toISOString();
    }
    return changeCargoShipmentTime({
      requestId: request.id,
      shipmentTime: time,
      status: request.status,
    });
  };

  return useMemo<ColumnProps<FeedCargoContentType>[]>(
    () => [
      {
        dataIndex: 'id',
        key: 'humanReadableId',
        title: t.Monitor.id,
        align: 'center',
        width: 150,
        fixed: 'left',
        sorter: true,
        render: (id: string, request) => (
          <div className={styles.outer}>
            <span>{request.humanReadableId}</span>
          </div>
        ),
      },
      {
        dataIndex: 'status',
        key: 'status',
        title: t.Monitor.status,
        align: 'center',
        fixed: 'left',
        width: 385,
        render: (status, request) => {
          const currentStatus = statuses.find(item => item.name === status);
          const statusName = currentStatus ? currentStatus.rusName : '-';
          const statusValue = currentStatus ? currentStatus.name : '-';
          return (
            <Descriptions size="small" column={1}>
              <Descriptions.Item key={uuid()}>
                <ChangedFieldsForm
                  key={uuid()}
                  id={request.id}
                  label=""
                  description={statusName}
                  status={statusValue}
                  transportType={request.transportType}
                  departureAddressCoordinates={{ latitude: 0, longitude: 0 }}
                />
              </Descriptions.Item>
            </Descriptions>
          );
        },
      },

      {
        dataIndex: 'transferTime',
        key: 'transferTime',
        title: t.Monitor.transferTime,
        align: 'center',
        width: 150,
        render: (transferTime, request) => request.status === 'CARGO_TRANSFER_FINISHED' ? (
          <DatePicker
            value={moment(transferTime)}
            disabledDate={d => d.isBefore(moment(request.desiredDate))}
            format={DATE_FORMAT.BASE}
            onChange={changedValue => handleTransferTimeChanged(changedValue, request)}
          />
        ) : (
          <div className={styles.outer}>
            <span>{formatTime(transferTime)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'shipmentTime',
        key: 'shipmentTime',
        title: t.Monitor.shipmentTime,
        align: 'center',
        width: 150,
        render: (shipmentTime, request) => request.status === 'CARGO_SHIPMENT_FINISHED' ? (
          <DatePicker
            value={moment(shipmentTime)}
            disabledDate={d => d.isBefore(moment(request.desiredDate))}
            format={DATE_FORMAT.BASE}
            onChange={changedValue => handleShipmentTimeChanged(changedValue, request)}
          />
        ) : (
          <div className={styles.outer}>
            <span>{formatTime(shipmentTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'sender',
        key: 'sender',
        title: t.Monitor.sender,
        align: 'center',
        width: 200,
        render: sender => (
          <div className={styles.outer}>
            <span>{sender}</span>
          </div>
        ),
      },
      {
        dataIndex: 'addresses',
        key: 'senderAddress',
        title: t.Monitor.senderAddress,
        align: 'center',
        width: 200,
        render: cellValue => renderAddress(formatAddress(cellValue[0])),
      },
      {
        dataIndex: 'recipient',
        key: 'recipient',
        title: t.Monitor.recipient,
        align: 'center',
        width: 200,
        render: recipient => (
          <div className={styles.outer}>
            <span>{recipient}</span>
          </div>
        ),
      },
      {
        dataIndex: 'addresses',
        key: 'recipientAddress',
        title: t.Monitor.recipientAddress,
        align: 'center',
        width: 200,
        render: cellValue => renderAddress(formatAddress(cellValue[cellValue.length - 1])),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, statuses]
  );
};
