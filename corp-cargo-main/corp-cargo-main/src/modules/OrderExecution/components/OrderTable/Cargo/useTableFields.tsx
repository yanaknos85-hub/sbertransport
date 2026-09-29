import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import moment, { Moment } from 'moment';
import { Link } from 'react-router-dom';
import { ColumnProps } from 'antd/lib/table';
import { DeleteOutlined } from '@ant-design/icons';
import { DATE_FORMAT, emptySign } from 'constants/constants.app';
import { useTransportTypes } from 'api/transport-types';
import { FeedCargoContentType } from 'stores/Engineer/Models/FeedCargo/Feed.content';
import { DatePicker, Descriptions } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import uuid from 'utils/uuid';
import { useChangeCargoShipmentTime, useChangeCargoTransferTime } from 'api/engineer';
import { convertToRubles } from 'utils/convertToRubles';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatVolume } from 'utils/formatVolume';
import { formatDistanceValue } from 'utils/formatDistance';
import { Statuses, CANCEL_ORDER_STATUSES, STATUSES } from '../../../constants/Cargo/Cargo';
import { EditableField } from '../../EditableField';
import { Source } from 'modules/OrderExecution/interfaces/Orders.types';
import * as routes from 'constants/constants.routes';

import styles from 'modules/Engineers/Engineer.module.scss';

export const renderAddress = (addressString: string): JSX.Element => (
  <div className={styles.outer}>
    <span>{addressString}</span>
  </div>
);

interface Params {
  setOrderId: (param: string) => void;
  setModalVisible:  (param: boolean) => void;
}

export const useTableFields: ({ setOrderId, setModalVisible }: Params) => ColumnProps<FeedCargoContentType>[] = (({ setOrderId, setModalVisible }: Params) => {
  const { t } = useTranslation();

  const { cargoStore } = useAppStoreContext();

  const { data: transportTypes } = useTransportTypes();
  const [changeCargoTransferTime] = useChangeCargoTransferTime();
  const [changeCargoShipmentTime] = useChangeCargoShipmentTime();

  const handleTransferTimeChanged = async (value: Moment | null, request: FeedCargoContentType) => {
    let time;
    if (value === null) {
      time = '';
    } else {
      time = moment(value).toISOString();
    }
    await changeCargoTransferTime({
      requestId: request.id,
      transferTime: time,
      status: request.status,
      source: request.source,
    });
    await cargoStore.getCargoOrderListDeferredPost();
  };

  const handleShipmentTimeChanged = async (value: Moment | null, request: FeedCargoContentType) => {
    let time;
    if (value === null) {
      time = '';
    } else {
      time = moment(value).toISOString();
    }
    await changeCargoShipmentTime({
      requestId: request.id,
      shipmentTime: time,
      status: request.status,
      source: request.source,
    });
    await cargoStore.getCargoOrderListDeferredPost();
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
        sorter: (a, b) => a.humanReadableId.localeCompare(b.humanReadableId),
        sortDirections: ['ascend', 'descend'],
        render: (_id: string, request) => (
          <div className={styles.outer}>
            <Link
              className={styles.link}
              to={`${routes.ORDER_EXECUTION_CARGO}/${request.source}/${request.humanReadableId}`}
            >
              {request.humanReadableId}
            </Link>
          </div>
        ),
      },
      {
        dataIndex: 'routeId',
        key: 'routeId',
        title: t.Monitor.routeNumber,
        width: 150,
        render: (routeId, request) =>
          !routeId ? (
            <div className={styles.outer}>{emptySign}</div>
          ) : (
            <div className={styles.outer}>
              <Link
                className={styles.link}
                to={`${routes.PLANNER_JOURNAL}?tab=journal&routeNumber=${request.routeNumber}`}
              >
                {request?.routeNumber}
              </Link>
            </div>
          ),
      },
      {
        dataIndex: 'status',
        key: 'status',
        title: t.Monitor.status,
        width: 385,
        render: (status, request) => {
          const currentStatus = STATUSES.find(item => item.name === status);
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
                  transportType={request.cargoTransportType}
                  departureAddressCoordinates={{ latitude: 0, longitude: 0 }}
                  source={request.source}
                  withAvailableFutureStatus
                  routeId={request.routeId}
                  routeNumber={request.routeNumber}
                />
              </Descriptions.Item>
            </Descriptions>
          );
        },
        shouldCellUpdate: () => false,
      },

      {
        dataIndex: 'cargoTransportType',
        key: 'cargoTransportType',
        title: t.Monitor.cargoTransportType,
        width: 155,
        render: transportType => {
          const currentTransportType = transportTypes.find(item => item.name === transportType);
          const transportTypeName = currentTransportType ? currentTransportType.rusName : emptySign;
          return (
            <div className={styles.outer}>
              <span>{transportTypeName}</span>
            </div>
          );
        },
      },

      {
        dataIndex: 'transferTime',
        key: 'transferTime',
        title: t.Monitor.transferStartTime,
        width: 200,
        render: (transferTime, request) => request.status === Statuses.CARGO_TRANSFER_FINISHED ? (
          <DatePicker
            allowClear={false}
            showSecond={false}
            showNow={false}
            showTime={{ format: 'HH:mm' }}
            value={moment(transferTime || new Date()).utc()}
            format={DATE_FORMAT.DATE_WITH_TIME_LEADING_ZEROS}
            onChange={changedValue => handleTransferTimeChanged(changedValue, request)}
            disabledDate={d => (moment(request.desiredDate).startOf('day') > d) || (d > moment())}
          />
        ) : (
          <div className={styles.outer}>
            <span>{transferTime ? moment(transferTime).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'shipmentTime',
        key: 'shipmentTime',
        title: t.Monitor.shipmentTime,
        width: 200,
        render: (shipmentTime, request) => request.status === Statuses.CARGO_SHIPMENT_FINISHED ? (
          <DatePicker
            allowClear={false}
            showSecond={false}
            showNow={false}
            showTime={{ format: 'HH:mm' }}
            format={DATE_FORMAT.DATE_WITH_TIME_LEADING_ZEROS}
            value={shipmentTime ? moment(shipmentTime).utc() : null}
            onChange={changedValue => handleShipmentTimeChanged(changedValue, request)}
            disabledDate={d => (moment(request.transferTime).startOf('day') > d) || (d > moment())}
          />
        ) : (
          <div className={styles.outer}>
            <span>{shipmentTime ? moment(shipmentTime).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'author',
        key: 'author',
        title: t.Monitor.author,
        width: 150,
        render: author => (
          <div className={styles.outer}>
            <span>{author}</span>
          </div>
        ),
      },

      {
        dataIndex: 'authorPhone',
        key: 'authorPhone',
        title: t.Monitor.authorPhone,
        width: 190,
        render: authorPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(authorPhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'desiredDate',
        key: 'desiredDate',
        title: t.Monitor.desiredTimeDate,
        width: 150,
        render: desiredDate => (
          <div className={styles.outer}>
            <span>{moment(desiredDate).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'contractor',
        key: 'contractor',
        title: t.Monitor.contractor,
        width: 210,
        render: contractor => (
          <div className={styles.outer}>
            <span>{contractor?.name ? contractor?.name : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'controlDate',
        key: 'controlDate',
        title: t.Monitor.controlDate,
        width: 150,
        render: controlDate => (
          <div className={styles.outer}>
            <span>{controlDate ? moment(controlDate).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS) : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'plannedPrice',
        key: 'plannedPrice',
        title: t.Monitor.plannedPrice,
        width: 200,
        render: plannedPrice => (
          <div className={styles.outer}>
            <span>{convertToRubles(plannedPrice).toString().replace('.', ',')}</span>
          </div>
        ),
      },

      {
        dataIndex: 'plannedRange',
        key: 'plannedRange',
        title: t.Monitor.plannedRange,
        width: 200,
        render: plannedRange => (
          <div className={styles.outer}>
            <span>{formatDistanceValue(plannedRange)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'sender',
        key: 'sender',
        title: t.Monitor.sender,
        width: 200,
        render: sender => (
          <div className={styles.outer}>
            <span>{sender}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderPhone',
        key: 'senderPhone',
        title: t.Monitor.senderPhone,
        width: 190,
        render: senderPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(senderPhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderAddress',
        key: 'senderAddress',
        title: t.Monitor.senderAddress,
        width: 260,
        render: senderAddress => (
          <div className={styles.outer}>
            <span>{senderAddress}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderOrganization',
        key: 'senderOrganization',
        title: t.Monitor.senderOrganization,
        width: 150,
        render: senderOrganization => (
          <div className={styles.outer}>
            <span>{senderOrganization}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipient',
        key: 'recipient',
        title: t.Monitor.recipient,
        width: 200,
        render: recipient => (
          <div className={styles.outer}>
            <span>{recipient}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientPhone',
        key: 'recipientPhone',
        title: t.Monitor.recipientPhone,
        width: 190,
        render: recipientPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(recipientPhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientAddress',
        key: 'recipientAddress',
        title: t.Monitor.recipientAddress,
        width: 260,
        render: recipientAddress => (
          <div className={styles.outer}>
            <span>{recipientAddress}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientOrganization',
        key: 'recipientOrganization',
        title: t.Monitor.recipientOrganization,
        width: 150,
        render: recipientOrganization => (
          <div className={styles.outer}>
            <span>{recipientOrganization}</span>
          </div>
        ),
      },

      {
        dataIndex: 'expected',
        key: 'expected',
        title: t.Monitor.waypointCount,
        width: 100,
        render: expected => (
          <div className={styles.outer}>
            <span>{expected?.waypointsCount ? expected?.waypointsCount : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'cargoTypes',
        key: 'cargoTypes',
        title: t.Monitor.cargoTypes,
        width: 150,
        render: cargoTypes => (
          <div className={styles.outer}>
            <span title={cargoTypes}>
              {cargoTypes ? (cargoTypes.length > 15 ? `${cargoTypes.slice(0, 15)}...` : cargoTypes) : emptySign}
            </span>
          </div>
        ),
      },

      {
        dataIndex: 'loaders',
        key: 'loaders',
        title: t.Monitor.loaders,
        width: 150,
        render: loaders => (
          <div className={styles.outer}>
            <span>{loaders || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'weight',
        key: 'weight',
        title: t.Monitor.weight,
        width: 150,
        render: weight => (
          <div className={styles.outer}>
            <span>{weight ? weight.replace('.', ',') : emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'volume',
        key: 'volume',
        title: t.Monitor.volume,
        width: 150,
        render: volume => (
          <div className={styles.outer}>
            <span>{formatVolume(volume).replace('.', ',')}</span>
          </div>
        ),
      },

      {
        dataIndex: 'creationTime',
        key: 'creationDate',
        title: t.Monitor.creationTimeDate,
        width: 150,
        render: creationTime => (
          <div className={styles.outer}>
            <span>{moment(creationTime).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'commonComment',
        key: 'commonComment',
        title: t.Monitor.commonComment,
        width: 150,
        render: commonComment => (
          <div className={styles.outer}>
            <span title={commonComment}>
              {commonComment ? (commonComment.length > 20 ? `${commonComment.slice(0, 20)}...` : commonComment) : emptySign}
            </span>
          </div>
        ),
      },

      {
        dataIndex: 'template',
        key: 'template',
        title: t.Monitor.template,
        width: 200,
        render: template => (
          <div className={styles.outer}>
            <span>{template || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'source',
        key: 'source',
        title: t.Monitor.source,
        width: 170,
        render: source => (
          <div className={styles.outer}>
            <span>{source}</span>
          </div>
        ),
      },
      {
        dataIndex: 'cancelOrder',
        key: 'cancelOrder',
        title: t.Monitor.cancelOrder,
        width: 150,
        render: (_, request) => {
          return (
            <>
              {
                CANCEL_ORDER_STATUSES.includes(request?.status as Statuses) && request.source !== Source.HOME_CLICK ? (
                  <DeleteOutlined
                    onClick={() => {
                      setOrderId(request.id);
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
});
