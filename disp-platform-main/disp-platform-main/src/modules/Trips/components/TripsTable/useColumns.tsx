import React, {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { Link } from 'react-router-dom';
import moment from 'moment';
import { useAppStore } from 'ioc';
import { ColumnType } from 'antd/lib/table';
import { Space, Tooltip } from 'antd';
import {
  EditOutlined, InfoCircleOutlined, SearchOutlined, StarFilled
} from '@ant-design/icons';

import { useAPIQueryCache } from 'api';
import { useProfile } from 'api/profile/profile.api';
import { useCreateShifts } from 'api/schedule2.0/schedule.api';
import {
  PassRequest, PassTrip, TypeOfAction, Waypoint
} from 'api/trips/trips.types';
import {
  PASS_TRIPS_KEY,
  TRIPS_STATISTIC_KEY,
  useAssignTripToDispatcher,
  useEditTrip,
  useSetDriverToRequest
} from 'api/trips/trips.api';

import {
  ChildSeatTypes,
  ChildSeatTitles,
  GroupTransferClass,
  GroupTransferClassTitles,
  TAXI_REQUEST_STATUSES,
  TRIP_STATUSES,
  TaxiClass,
  TaxiClassDescriptions,
  TripStatuses
} from 'constants/trips.constants';
import { DirectionMapSortToOrder, EMPTY_CELL_CONTENT, TripTypes } from 'constants/app.constants';
import { TRIP } from 'constants/routes.constants';
import { useTranslation } from 'i18n';

import Flex from 'components/Flex/Flex';
import { Button } from 'components/Button';
import { SelectDrivers } from 'components/SelectDrivers/SelectDrivers';

import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { getFullName } from 'utils/getFullName';
import { getAddress } from 'utils/getAddress';
import { convertToRubles } from 'utils/convertToRubles';
import { formatRubles } from 'utils/formatRubles';
import { convertSnakeToCamelCase } from 'utils/convertStringCase';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';

import { Columns } from 'modules/Trips/constants';
import { useTripsQuery } from 'modules/Trips/context/TripsQuery';
import { useTripsModal } from 'modules/Trips/context/TripsModal';
import { isDriverAssigned, getRequestIcon } from './utils';
import { Address } from '../Addresses/Addresses';

import { StatusWithConfirm } from '../StatusWithConfirm/StatusConfirm';
import VehicleCell from './VehicleCell';

import { ReactComponent as EyeVisibleIcon } from 'assets/icons/eye-visible.svg';
import { ReactComponent as EyeInVisibleIcon } from 'assets/icons/eye-invisible.svg';
import { ReactComponent as PhoneIcon } from 'assets/icons/phone.svg';

import styles from './index.module.scss';

type OptimizeColumns = (columns: ColumnType<PassTrip>[]) => ColumnType<PassTrip>[];

const PASS_INTERMEDIATE_ADDRESSES = 'PASS_INTERMEDIATE_ADDRESSES';

export const useColumns = (getOptimizeColumns: OptimizeColumns) => {
  const { logger } = useAppStore();
  const { t } = useTranslation();
  const cache = useAPIQueryCache();
  const { openSetDriver, openEdit } = useTripsModal();
  const { contractorId } = useProfile().data;
  const [assignTrip, { isLoading }] = useAssignTripToDispatcher(contractorId);
  const { field, direction } = useTripsQuery().query;

  const [editTrip, { isLoading: isLoadingStatus }] = useEditTrip(contractorId);
  const [setDriver, { isLoading: isLoadingSetDriver }] = useSetDriverToRequest(contractorId);
  const [createShift, { isLoading: isLoadingShift }] = useCreateShifts(contractorId);

  const [showAddresses, setShowAddress] = useState(localStorage.getItem(PASS_INTERMEDIATE_ADDRESSES) === 'true');

  const changeStatus = useCallback((tripId: UUID, value: TRIP_STATUSES) => {
    editTrip({
      tripId,
      data: [{ field: 'status', value }],
    })
      .then(() => logger.toMessage('success', 'Статус успешно изменен'))
      .catch(ignore);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editTrip, logger, contractorId]);

  const handleSetDriver = useCallback(({
    driverId,
    planningShiftId,
    tripId,
  }: {
    driverId: UUID;
    planningShiftId: UUID;
    tripId: UUID;
  }) => {
    setDriver({
      tripId,
      driverId,
      planningShiftId,
    })
      .then(() => logger.toMessage('success', 'Водитель успешно назначен'))
      .then(() => {
        cache.invalidateQueries([PASS_TRIPS_KEY]);
        cache.invalidateQueries([TRIPS_STATISTIC_KEY]);
      })
      .catch(ignore);
  }, [cache, logger, setDriver]);

  const setDriverToOrder = useCallback((driverId: string, trip: PassTrip) => {
    createShift([{
      index: 0,
      driverId: driverId as UUID,
      // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
      vehicleId: trip.vehicle?.id!,
      startDate: moment(trip.expectedStartTime).clone().utc().startOf('minute').format(),
      endDate: moment(trip.expectedEndTime).clone().utc().endOf('minute').format(),
    }])
      .then(shifts => shifts && ('id' in shifts[0]) && handleSetDriver({
        driverId: driverId as UUID,
        planningShiftId: shifts[0].id,
        tripId: trip.id,
      }))
      .catch(ignore);
  }, [createShift, handleSetDriver]);

  const onShowAddress = useCallback(() => {
    setShowAddress(visible => !visible);
  }, []);

  useEffect(() => {
    localStorage.setItem(PASS_INTERMEDIATE_ADDRESSES, JSON.stringify(showAddresses));
  }, [showAddresses]);

  const startFixedColumns = useMemo(
    (): ColumnType<PassTrip>[] => [
      {
        key: Columns.Icon,
        width: 25,
        render: (_, trip) => getRequestIcon(trip),
        fixed: 'left',
      },
      {
        title: '',
        key: Columns.Number,
        render: (value, record) => (
          <Space size={15}>
            <EditOutlined
              className={styles.icon}
              onClick={() => openEdit(record)}
            />

            <Link to={TRIP.replace(':id', record.id)} onClick={e => e.stopPropagation()}>
              <SearchOutlined className={styles.icon} />
            </Link>
          </Space>
        ),
        width: 80,
        fixed: 'left',
      },
    ],
    [openEdit]
  );

  const passColumns = useMemo(
    (): ColumnType<PassTrip>[] => [
      {
        title: t.Requests.Columns.HumanReadableId,
        dataIndex: Columns.HumanReadableId,
        key: Columns.HumanReadableId,
        width: 150,
        render: (humanReadableId, record) => (
          <div className={styles.id} onClick={() => openSetDriver(record)}>
            {humanReadableId}
          </div>
        ),
      },
      {
        title: t.Requests.Columns.Requests,
        dataIndex: Columns.Requests,
        key: Columns.Requests,
        width: 160,
        render: (requests, record) => requests.length ? (
          <div className={styles.id}>
            {[...requests]
              .filter((request: PassRequest) => request.status !== TAXI_REQUEST_STATUSES.TAXI_CANCELLED)
              .reverse()
              .map((request: PassRequest) => (
                <div key={request.id}>
                  {request.humanReadableId}
                  <br />
                </div>
              ))}
          </div>
        ) : (
          <div className={styles.id}>
            {record.externalHumanReadableId}
          </div>
        ),
      },
      {
        title: t.Requests.Columns.Status,
        dataIndex: Columns.Status,
        key: Columns.Status,
        render: (status, record) => (
          <StatusWithConfirm
            tripMode={TripTypes.Passenger}
            isEdit
            loading={isLoadingStatus}
            value={status}
            onChange={newStatus => changeStatus(record.id, newStatus as TRIP_STATUSES)}
            className={styles.statusSelect}
          />
        ),
        width: 200,
      },
      {
        title: t.Requests.Columns.ExpectedStartTime,
        dataIndex: Columns.StartTime,
        key: Columns.StartTime,
        render: startTime => {
          if (!startTime) {
            // для удаленных записей startTime может быть undefined
            return null;
          }

          return (
            <>
              <p className={styles.date}>
                По клиенту:
                <span className={styles.tripClientDate}>
                  {moment.parseZone(startTime).format('DD.MM.YYYY HH:mm ([GMT]Z)')}
                </span>
              </p>
              <p className={styles.date}>
                По диспетчеру:
                <span className={styles.dispDate}>
                  {moment(startTime).format('DD.MM.YYYY HH:mm')}
                </span>
              </p>
            </>
          );
        },
        width: 310,
        sortDirections: ['ascend', 'descend'],
        sorter: true,
        sortOrder: (
          field && convertSnakeToCamelCase(field) === Columns.StartTime && direction
            ? DirectionMapSortToOrder[direction]
            : null
        ),
      },
      {
        title: () => (
          <div className={styles.expectedTime}>
            {t.Requests.Columns.ExpectedTime}
          </div>
        ),
        dataIndex: 'expectedTime',
        key: Columns.ExpectedTime,
        render: (expectedTime: number) => (
          expectedTime ? Math.round(expectedTime / 60) : EMPTY_CELL_CONTENT
        ),
        width: 200,
      },
      {
        title: t.Requests.Columns.Vehicle,
        dataIndex: Columns.Vehicle,
        key: Columns.Vehicle,
        render: (_vehicle, trip) => <VehicleCell trip={trip} />,
        width: 240,
      },
      {
        title: t.Requests.Columns.Driver,
        dataIndex: Columns.Driver,
        key: Columns.Driver,
        render: (_driver, record) => {
          const driver = _driver ?? record.planned?.driver;

          return driver ? (
            <div>
              <Flex gap={10} alignItems="center">
                <span>{getFullName(driver)}</span>
                {!!driver.rating && (
                  <span>
                    <StarFilled />
                    {' '}
                    {(driver.rating / 100).toFixed(2)}
                    {' '}
                  </span>
                )}
                {!!record.planned && (
                  <Tooltip title={t.Drivers.driverPlanned}>
                    <InfoCircleOutlined size={10} />
                  </Tooltip>
                )}
              </Flex>
              <div>{driver.contactPhone}</div>
            </div>
          ) : record.vehicle || record.planned?.vehicle ? (
            <SelectDrivers
              type={record.vehicle?.vehicleType ?? record.planned?.vehicle.vehicleType}
              fetchOnOpen
              onChange={driverId => setDriverToOrder(driverId as string, record)}
              loading={isLoadingShift || isLoadingSetDriver}
            />
          ) : (
            EMPTY_CELL_CONTENT
          );
        },
        width: 260,
      },
      {
        title: t.Requests.Columns.Passenger,
        dataIndex: 'requests',
        key: Columns.Passenger,
        render: (requests, record) => requests.length ? (
          [...requests]
            .filter((request: PassRequest) => request.status !== TAXI_REQUEST_STATUSES.TAXI_CANCELLED)
            .reverse()
            .map((request: PassRequest) => (
              <div key={request.id}>
                {request.passenger && (
                  <div>
                    {getFullName({
                      firstName: request.passenger.firstName,
                      patronymic: request.passenger.patronymic,
                    })}
                    <div>{request.passenger.mobilePhone}</div>
                  </div>
                )}
              </div>
            ))
        ) : (
          record.waypoints.map(w => {
            const name = w.contact?.name;
            const phone = w.contact?.phone;

            return name || phone ? (
              <div key={w.index}>
                {name}
                <div>{phone}</div>
              </div>
            ) : w.passengers?.length ? (
              <div key={w.index}>
                {w.passengers.map((passenger, idx) => passenger.type === TypeOfAction.BOARDING && (
                  <div key={`passenger-${idx}`}>
                    {getFullName({
                      firstName: passenger.firstName,
                      patronymic: passenger.patronymic,
                    })}
                    <div>{passenger.phone}</div>
                  </div>
                ))}
              </div>
            ) : (
              EMPTY_CELL_CONTENT
            );
          })
        ),
        width: 200,
      },
      {
        title: t.Requests.Columns.AddressFrom,
        dataIndex: 'waypoints',
        key: Columns.AddressFrom,
        render: (waypoints: Waypoint[]) => waypoints?.[0] && (waypoints[0].fullAddress ?? getAddress(waypoints[0])),
        width: 250,
      },
      {
        title: t.Requests.Columns.AddressTo,
        dataIndex: 'waypoints',
        key: Columns.AddressTo,
        render: (waypoints: Waypoint[]) => waypoints?.at(-1) && (
          waypoints.at(-1)!.fullAddress ?? getAddress(waypoints.at(-1)!)
        ),
        width: 250,
      },
      {
        title: () => (
          <Flex gap={10} alignItems="center">
            {t.Requests.Columns.IntermediateAddresses}
            {showAddresses ? (
              <EyeVisibleIcon className={styles.eyeIcon} onClick={onShowAddress} />
            ) : (
              <EyeInVisibleIcon className={styles.eyeIcon} onClick={onShowAddress} />
            )}
          </Flex>
        ),
        dataIndex: 'waypoints',
        key: Columns.IntermediateAddresses,
        render: waypoints => waypoints?.length > 2 ? (
          showAddresses ? (
            waypoints
              .slice(1, -1)
              .map((waypoint: Waypoint) => (
                <div key={waypoint.index}>{waypoint.fullAddress ?? getAddress(waypoint)}</div>
              ))
          ) : (
            <Address waypoints={waypoints.slice(1, -1)} />
          )
        ) : (
          EMPTY_CELL_CONTENT
        ),
        width: 260,
      },
      {
        title: t.Requests.Columns.CommentForDriver,
        dataIndex: 'requests',
        key: Columns.CommentForDriver,
        render: (requests, record) => {
          const _requests = [...requests]
            .filter((request: PassRequest) => request.status !== TAXI_REQUEST_STATUSES.TAXI_CANCELLED)
            .reverse();

          return _requests.length ? (
            _requests.map((request: PassRequest, index: number) => (
              <div key={request.id}>
                {_requests.length > 1 && (
                  <span>
                    {index + 1}
                    .
                  </span>
                )}
                {' '}
                {request.commentForDriver ?? request.comment ?? EMPTY_CELL_CONTENT}
              </div>
            ))
          ) : (
            <div>{record.comment}</div>
          );
        },
        width: 250,
      },
      {
        title: t.Requests.Columns.PassengerCount,
        dataIndex: Columns.PassengerCount,
        key: Columns.PassengerCount,
        width: 150,
      },
      {
        title: t.Requests.Columns.ExpectedDistance,
        dataIndex: Columns.ExpectedDistance,
        key: Columns.ExpectedDistance,
        width: 180,
        render: (expectedDistance: number) => expectedDistance?.toFixed(2) ?? EMPTY_CELL_CONTENT,
      },
      {
        title: t.Requests.Columns.FactDistance,
        dataIndex: Columns.FactDistance,
        key: Columns.FactDistance,
        width: 100,
        render: (factDistance: number) => factDistance?.toFixed(2) ?? EMPTY_CELL_CONTENT,
      },
      {
        title: t.Requests.Columns.ExpectedCost,
        dataIndex: Columns.ExpectedCost,
        key: Columns.ExpectedCost,
        width: 180,
        render: (expectedCost: number) => (
          expectedCost ? formatRubles(convertToRubles(expectedCost)) : EMPTY_CELL_CONTENT
        ),
      },
      {
        title: t.Requests.Columns.DriverWaitingTime,
        dataIndex: Columns.DriverWaitingTime,
        key: Columns.DriverWaitingTime,
        width: 170,
        render: (driverWaitingTime: number) => (
          driverWaitingTime ? Math.round(driverWaitingTime / 60) : EMPTY_CELL_CONTENT
        ),
      },
      {
        title: t.Requests.Columns.TaxiClass,
        dataIndex: Columns.TaxiClass,
        key: Columns.TaxiClass,
        render: (taxiClass, record) => taxiClass
          ? TaxiClassDescriptions[taxiClass as TaxiClass] ?? taxiClass
          : record.requests[0]?.groupTransferClass
            ? GroupTransferClassTitles[record.requests[0].groupTransferClass as GroupTransferClass] ?? (
              record.requests[0].groupTransferClass
            )
            : EMPTY_CELL_CONTENT,
        width: 150,
      },
      {
        title: t.Requests.Columns.Dispatcher,
        dataIndex: Columns.Dispatcher,
        key: Columns.Dispatcher,
        render: (dispatcher, record) => dispatcher ? (
          getFullName(dispatcher)
        ) : !record.dispatcher?.phone && isDriverAssigned(record.status) ? (
          t.Requests.auto
        ) : (
          <Button
            onClick={() => assignTrip({ tripId: record.id })}
            loading={isLoading}
            disabled={TripStatuses[record.status].isFinal}
          >
            {t.Requests.assignToMe}
          </Button>
        ),
        width: 180,
      },
      {
        title: t.Requests.Columns.FlightNumber,
        dataIndex: 'information',
        key: Columns.FlightNumber,
        render: (_text, record) => {
          const info = record.information;
          return info?.numberFlight ?? EMPTY_CELL_CONTENT;
        },
        width: 150,
      },
      {
        title: t.Requests.Columns.AdditionalContact,
        dataIndex: 'information',
        key: Columns.AdditionalContact,
        render: (_text, record) => {
          const info = record.information;
          const fio = info?.addContactFIO;
          const phone = info?.addContactPhone;
          if (!fio && !phone) {
            return EMPTY_CELL_CONTENT;
          }
          return (
            <div>
              {fio && <div>{fio}</div>}
              {phone && (
                <Flex gap={4} alignItems="center">
                  <PhoneIcon />
                  <a
                    href={`tel:${phone}`}
                    style={{ color: '#000' }}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {formatPhoneNumber(phone)}
                  </a>
                </Flex>
              )}
            </div>
          );
        },
        width: 240,
      },
      {
        title: t.Requests.Columns.HotelNumber,
        dataIndex: 'information',
        key: Columns.HotelNumber,
        render: (_text, record) => record.information?.phoneHotel ?? EMPTY_CELL_CONTENT,
        width: 150,
      },
      {
        title: t.Requests.Columns.ChildSeat,
        dataIndex: 'information',
        key: Columns.ChildSeat,
        render: (_text, record) => {
          const info = record.information;
          if (!info?.childSeat) {
            return EMPTY_CELL_CONTENT;
          }
          const details = info.childSeatDetails;
          if (details) {
            const names = Object.entries(ChildSeatTitles)
              .filter(([key]) => details[key as ChildSeatTypes])
              .map(([, title]) => title);
            return names.length
              ? names.map((name, i) => <div key={i}>{name}</div>)
              : 'Да';
          }
          return 'Да';
        },
        width: 200,
      },
      {
        title: t.Requests.Columns.AnimalTransport,
        dataIndex: 'information',
        key: Columns.AnimalTransport,
        render: (_text, record) => {
          const info = record.information;
          if (!info?.animal) {
            return EMPTY_CELL_CONTENT;
          }
          return info.animalComment || 'Да';
        },
        width: 160,
      },
      {
        title: t.Requests.Columns.OversizedLuggage,
        dataIndex: 'information',
        key: Columns.OversizedLuggage,
        render: (_text, record) => {
          const info = record.information;
          if (!info?.bugsOversized) {
            return EMPTY_CELL_CONTENT;
          }
          return info.bugsOversizedComment || 'Да';
        },
        width: 160,
      },
    ],
    [
      t,
      field,
      direction,
      isLoading,
      isLoadingStatus,
      isLoadingSetDriver,
      isLoadingShift,
      showAddresses,
      onShowAddress,
      openSetDriver,
      assignTrip,
      setDriverToOrder,
      changeStatus,
    ]
  );

  return {
    passColumns: [
      ...startFixedColumns,
      ...getOptimizeColumns(passColumns),
    ].map((item, idx) => {
      if (item.key === Columns.HumanReadableId) {
        item.fixed = idx === startFixedColumns.length ? 'left' : undefined;
      }

      return item;
    }),
  };
};
