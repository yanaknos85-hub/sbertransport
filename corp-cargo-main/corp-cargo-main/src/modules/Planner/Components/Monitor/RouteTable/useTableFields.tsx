import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import moment, { Moment } from 'moment';
import { Link } from 'react-router-dom';
import { ColumnProps } from 'antd/lib/table';
import { CUBE_MM_TO_METERS, DATE_FORMAT, emptySign } from 'constants/constants.app';
import { formatBaseDate } from 'utils/formatTime';
import { DatePicker, Descriptions } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import uuid from 'utils/uuid';
import { useChangeCargoTransferTime, useChangeCargoShipmentTime } from 'api/planner';
import { convertToRubles } from 'utils/convertToRubles';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatDistanceValue } from 'utils/formatDistance';
import { MonitorRouteType } from '../../../types';
import { Statuses, STATUSES, CANCEL_ROUTE_STATUSES, StatusNames } from '../constants';
import { EditableField } from '../EditableField';
import { compareBy } from 'utils/sorting';
import { PLANNER_JOURNAL_DETAILED } from 'constants/constants.routes';

import styles from '../styles.module.scss';

interface Params {
  setRouteId: (routeId: string) => void;
  setModalVisible:  (param: boolean) => void;
}

export const renderAddress = (addressString: string): JSX.Element => (
  <div className={styles.outer}>
    <span>{addressString}</span>
  </div>
);

export const useTableFields: ({ setRouteId, setModalVisible }: Params) => ColumnProps<MonitorRouteType>[] = ({ setRouteId, setModalVisible }: Params) => {
  const { t } = useTranslation();

  const [changeCargoTransferTime] = useChangeCargoTransferTime();
  const [changeCargoShipmentTime] = useChangeCargoShipmentTime();
  const handleDesiredDateChanged = (value: Moment | null, request: MonitorRouteType) => {
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

  const handleShipmentTimeChanged = (value: Moment | null, request: MonitorRouteType) => {
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

  return useMemo<ColumnProps<MonitorRouteType>[]>(
    () => [
      {
        dataIndex: 'id',
        key: 'id',
        title: t.Planner.Monitor.id,
        align: 'center',
        width: 150,
        fixed: 'left',
        sorter: compareBy('humanReadableId'),
        render: (id: string, request) => (
          request.status !== StatusNames[Statuses.CARGO_CANCELED] ? (
            <div className={styles.outer}>
              <Link className={styles.link} to={`${PLANNER_JOURNAL_DETAILED}/${request.id}`}>
                {request.humanReadableId}
              </Link>
            </div>
          ) : (
            <div className={styles.outer}>
              {request.humanReadableId}
            </div>
          )
        ),
      },
      {
        dataIndex: 'status',
        key: 'status',
        title: t.Planner.Monitor.status,
        align: 'center',
        width: 385,
        render: (status, request) => {
          const currentStatus = STATUSES.find(item => item.rusName === status);
          const statusName = currentStatus ? currentStatus.rusName : emptySign;
          const statusValue = currentStatus ? currentStatus.name : emptySign;

          return (
            <Descriptions size="small" column={1}>
              <Descriptions.Item key={uuid()}>
                <EditableField
                  key={uuid()}
                  id={request.id}
                  label=""
                  description={statusName}
                  status={statusValue}
                  departureAddressCoordinates={{ latitude: 0, longitude: 0 }}
                />
              </Descriptions.Item>
            </Descriptions>
          );
        },
        shouldCellUpdate: () => false,
      },
      {
        dataIndex: 'author',
        key: 'author',
        title: t.Planner.Monitor.author,
        align: 'center',
        width: 150,
        render: author => (
          <div className={styles.outer}>
            <span>{author}</span>
          </div>
        ),
      },
      {
        dataIndex: 'creationTime',
        key: 'creationTime',
        title: t.Planner.Monitor.creationDate,
        align: 'center',
        width: 150,
        sorter: compareBy('creationTime'),
        render: creationTime => (
          <div className={styles.outer}>
            <span>{formatBaseDate(creationTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'desiredDate',
        key: 'desiredDate',
        title: t.Planner.Monitor.desiredDate,
        align: 'center',
        width: 150,
        sorter: compareBy('desiredDate'),
        render: (desiredDate, request) => request.status === 'CARGO_TRANSFER_FINISHED'
        || request.status === 'CARGO_SHIPMENT_FINISHED' ? (
          <DatePicker
            value={desiredDate ? moment(desiredDate) : null}
            disabledDate={d => d.isBefore(moment(request.desiredDate))}
            format={DATE_FORMAT.BASE_REVERTED}
            onChange={changedValue => handleDesiredDateChanged(changedValue, request)}
          />
          ) : (
            <div className={styles.outer}>
              <span>{formatBaseDate(desiredDate)}</span>
            </div>
          ),
      },
      {
        dataIndex: 'shipmentTime',
        key: 'shipmentTime',
        title: t.Planner.Monitor.shipmentTime,
        align: 'center',
        width: 160,
        sorter: compareBy('shipmentTime'),
        render: (shipmentTime, request) => request.status === 'CARGO_SHIPMENT_FINISHED' ? (
          <DatePicker
            value={shipmentTime ? moment(shipmentTime) : null}
            disabledDate={d => d.isBefore(moment(request.desiredDate))}
            format={DATE_FORMAT.BASE}
            onChange={changedValue => handleShipmentTimeChanged(changedValue, request)}
          />
        ) : (
          <div className={styles.outer}>
            <span>{formatBaseDate(shipmentTime)}</span>
          </div>
        ),
      },
      {
        dataIndex: 'weight',
        key: 'weight',
        title: t.Planner.Monitor.weight,
        align: 'center',
        width: 150,
        sorter: compareBy('weight'),
        render: weight => (
          <div className={styles.outer}>
            <span>{Number(weight?.toFixed(0)) || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'volume',
        key: 'volume',
        title: t.Planner.Monitor.volume,
        align: 'center',
        width: 150,
        sorter: compareBy('volume'),
        render: volume => {
          const renderVolume = volume
            ? volume / CUBE_MM_TO_METERS > 0.001
              ? Number(volume / CUBE_MM_TO_METERS)
                .toFixed(3)
                .toString()
                .replace('.', ',')
              : '0,001'
            : '-';
          return (
            <div className={styles.outer}>
              <span>{renderVolume}</span>
            </div>
          );
        },
      },
      {
        dataIndex: 'distance',
        key: 'distance',
        title: t.Planner.Monitor.distance,
        align: 'center',
        width: 200,
        sorter: compareBy('distance'),
        render: distance => (
          <div className={styles.outer}>
            <span>{formatDistanceValue(distance) || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'cost',
        key: 'cost',
        title: t.Planner.Monitor.cost,
        align: 'center',
        width: 200,
        sorter: compareBy('cost'),
        render: cost => (
          <div className={styles.outer}>
            <span>{convertToRubles(cost).toString().replace('.', ',') || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'waypointCount',
        key: 'waypointCount',
        title: t.Planner.Monitor.waypointCount,
        align: 'center',
        width: 150,
        sorter: compareBy('waypointCount'),
        render: waypointCount => (
          <div className={styles.outer}>
            <span>{waypointCount}</span>
          </div>
        ),
      },
      {
        dataIndex: 'contractorName',
        key: 'contractorName',
        title: t.Planner.Monitor.contractorName,
        align: 'center',
        width: 300,
        render: contractorName => (
          <div className={styles.outer}>
            <span>{contractorName || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'capacity',
        key: 'capacity',
        title: t.Planner.Monitor.capacity,
        align: 'center',
        width: 210,
        sorter: compareBy('capacity'),
        render: capacity => (
          <div className={styles.outer}>
            <span>{capacity || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'loaders',
        key: 'loaders',
        title: t.Planner.Monitor.loaders,
        align: 'center',
        width: 210,
        sorter: compareBy('loaders'),
        render: loaders => (
          <div className={styles.outer}>
            <span>{loaders || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'driver',
        key: 'driver',
        title: t.Planner.Monitor.driver,
        align: 'center',
        width: 150,
        render: driver => (
          <div className={styles.outer}>
            <span>{driver || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'driverPhone',
        key: 'driverPhone',
        title: t.Planner.Monitor.driverPhone,
        align: 'center',
        width: 180,
        render: driverPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(driverPhone) || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'auto',
        key: 'auto',
        title: t.Planner.Monitor.auto,
        align: 'center',
        width: 200,
        render: auto => (
          <div className={styles.outer}>
            <span>{auto || '-'}</span>
          </div>
        ),
      },
      {
        dataIndex: 'cancelRoute',
        key: 'cancelRoute',
        title: t.Planner.cancelRoute,
        width: 150,
        render: (_, request) => {
          return (
            <>
              {
                CANCEL_ROUTE_STATUSES.includes(request?.status as Statuses) ? (
                  <DeleteOutlined
                    onClick={() => {
                      setRouteId(request.id);
                      setModalVisible(true);
                    }}
                  />
                ) : null
              }
            </>
          )
        },
      },
    ],
    [t]
  );
};
