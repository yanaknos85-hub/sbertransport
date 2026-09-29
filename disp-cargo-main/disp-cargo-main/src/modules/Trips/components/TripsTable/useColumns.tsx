import React, {
  FC, useCallback, useEffect, useMemo, useState
} from 'react';
import { ColumnType } from 'antd/lib/table';
import Tooltip from 'antd/lib/tooltip';
import { useTranslation } from 'i18n';
import moment from 'moment';
import { getFullName } from 'utils/getFullName';
import { ReactComponent as Lightning } from 'assets/icons/lightning.svg';
import { ReactComponent as Fire } from 'assets/icons/fire.svg';
import { Button } from 'components/Button';
import { EditOutlined, InfoCircleOutlined, StarFilled } from '@ant-design/icons';
import { isDriverAssigned, isRequestDeadline } from './utils';
import { useTripsModal } from '../../context/TripsModal';
import styles from './index.module.scss';
import { getAddress } from 'utils/getAddress';
import { useTripsQuery } from '../../context/TripsQuery';
import { Columns, columnToSortMap } from '../../constants';
import { UUID } from 'utils/io-ts';
import { useAppStore } from 'ioc';
import { StatusSelect } from 'components/StatusSelect';
import Flex from 'components/Flex/Flex';
import {
  CargoRequest, CargoTrip, RequestType, Waypoint
} from 'api/trips-cargo/trips-cargo.types';
import { TRIP_STATUSES, TripStatuses } from 'constants/trips.constants';
import { useProfile } from 'api/profile/profile.api';
import { useAssignCargoTripToDispatcher, useEditCargoTrip } from 'api/trips-cargo/trips-cargo.api';
import { ignore } from 'utils/utils';
import {
  CUBE_CM_TO_METERS, DirectionMapSortToOrder, EMPTY_CELL_CONTENT, TripTypes
} from 'constants/app.constants';
import { Address } from '../Addresses/Addresses';
import { Comment } from '../Comment/Comment';

import { ReactComponent as EyeVisibleIcon } from 'assets/icons/eye-visible.svg';
import { ReactComponent as EyeInVisibleIcon } from 'assets/icons/eye-invisible.svg';

const getRequestIcon = (trip: CargoTrip) => isRequestDeadline(trip) ? <Fire /> : trip.status === TRIP_STATUSES.WAITING_FOR_ASSIGNMENT ? <Lightning /> : '';

const CARGO_INTERMEDIATE_ADDRESSES = 'CARGO_INTERMEDIATE_ADDRESSES';

const CargoName: FC<{ request: CargoRequest; requests: CargoRequest[]; index: number }> = ({
  request,
  requests,
  index,
}) => (
  <div>
    {requests.length > 1 && <span>{index + 1}</span>}
    {' '}
    {request.cargoData?.map((cargo, cargoIndex) => (
      <span key={cargo.id}>
        {cargo.name}
        {cargoIndex !== (request.cargoData?.length ?? 0) - 1 && ' ,'}
      </span>
    ))}
  </div>
);

export const useColumns = () => {
  const { logger } = useAppStore();
  const { t } = useTranslation();
  const { openSetDriver, openEdit } = useTripsModal();
  const { contractorId } = useProfile().data;
  const [assignTrip, { isLoading }] = useAssignCargoTripToDispatcher(contractorId);
  const { field, direction } = useTripsQuery().query;

  const [showAddresses, setShowAddress] = useState(localStorage.getItem(CARGO_INTERMEDIATE_ADDRESSES) === 'true');

  const [editTrip, { isLoading: isLoadingStatus }] = useEditCargoTrip(contractorId);

  const changeStatus = useCallback((tripId: UUID, value: TRIP_STATUSES) => {
    editTrip({
      tripId,
      data: [{ field: 'status', value }],
    })
      .then(() => logger.toMessage('success', 'Статус успешно изменен'))
      .catch(ignore);
  }, [editTrip, logger]);

  const onShowAddress = useCallback(() => {
    setShowAddress(visible => !visible);
  }, []);

  useEffect(() => {
    localStorage.setItem(CARGO_INTERMEDIATE_ADDRESSES, JSON.stringify(showAddresses));
  }, [showAddresses]);

  const cargoColumns = useMemo(
    (): ColumnType<CargoTrip>[] => [
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
          <div>
            <EditOutlined className={styles.edit} onClick={() => openEdit(record)} />
          </div>
        ),
        width: 40,
        fixed: 'left',
      },
      {
        title: t.Requests.ColumnsCargo.RouteId,
        dataIndex: Columns.RouteHumanReadableId,
        width: 160,
        render: (routeHumanReadableId, record) => (
          <div className={styles.id} onClick={() => openSetDriver(record)}>
            {routeHumanReadableId}
          </div>
        ),
        fixed: 'left',
      },
      {
        title: t.Requests.ColumnsCargo.Requests,
        dataIndex: Columns.Requests,
        width: 140,
        render: (requests, record) => (
          <div className={styles.id}>
            {requests.length ? (
              [...requests]
                .reverse()
                .map((request: CargoRequest) => (
                  <div key={request.id}>
                    {request.humanReadableId}
                    <br />
                  </div>
                ))
            ) : (
              record.waypoints.map(waypoint => (
                waypoint.contacts?.map(contact => (
                  contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                    <div key={request.humanReadableId}>
                      {request.humanReadableId}
                      <br />
                    </div>
                  ))
                ))
              ))
            )}
          </div>
        ),
      },
      {
        title: t.Requests.ColumnsCargo.ID,
        dataIndex: Columns.HumanReadableId,
        width: 150,
        render: (humanReadableId, record) => (
          <div className={styles.id} onClick={() => openSetDriver(record)}>
            {humanReadableId}
          </div>
        ),
      },
      {
        title: t.Requests.ColumnsCargo.Status,
        dataIndex: Columns.Status,
        render: (status, record) => (
          <StatusSelect
            tripMode={TripTypes.Cargo}
            isEdit
            loading={isLoadingStatus}
            value={status}
            onChange={newStatus => changeStatus(record.id, newStatus as TRIP_STATUSES)}
            className={styles.statusSelect}
            data-name="platform_status_route"
          />
        ),
        width: 200,
      },
      {
        title: t.Requests.ColumnsCargo.StartTime,
        dataIndex: Columns.StartTime,
        render: (startTime, record) => {
          if (!startTime) {
            // для удаленных записей startTime может быть undefined
            return null;
          }

          return (
            <>
              <p className={styles.date}>
                По клиенту:
                <span className={styles.clientDate}>
                  {moment.parseZone(startTime).format('DD.MM.YYYY HH:mm ([GMT]Z)')}
                </span>
              </p>
              <p className={styles.date}>
                Погрузка:
                <span className={styles.loadDate}>
                  {moment.parseZone(record.dispatcherStartTime).format('DD.MM.YYYY HH:mm ([GMT]Z)')}
                </span>
              </p>
            </>
          );
        },
        width: 310,
        sortDirections: ['ascend', 'descend'],
        sorter: true,
        defaultSortOrder:
          field === columnToSortMap[Columns.StartTime] && direction ? DirectionMapSortToOrder[direction] : null,
      },
      {
        title: t.Requests.ColumnsCargo.Driver,
        dataIndex: Columns.Driver,
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
          ) : (
            EMPTY_CELL_CONTENT
          );
        },
        width: 260,
      },
      {
        title: t.Requests.ColumnsCargo.Sender,
        dataIndex: 'requests',
        key: Columns.Sender,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {x.sender?.organization || '-'}
            </div>
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                <div key={request.humanReadableId}>
                  {request.organization || '-'}
                  <br />
                </div>
              ))
            ))
          ))
        ),
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.SenderContact,
        dataIndex: 'requests',
        key: Columns.SenderContact,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {getFullName({ firstName: x.sender?.firstName ?? '', patronymic: x.sender?.patronymic ?? '' })}
              <br />
              {x.sender?.phone ?? x.sender?.mobilePhone}
            </div>
          ))
        ) : (
          record.waypoints
            .filter(waypoint => waypoint.contacts?.some(contact => (
              contact.requests.some(request => request.type === RequestType.Load)
            )))
            .map(waypoint => (
              waypoint.contacts?.map(contact => (
                <div key={`${contact.contact.phone}${waypoint.index}`}>
                  {contact.contact.fullName}
                  <br />
                  {contact.contact.phone}
                  <br />
                </div>
              ))
            ))
        ),
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.AddressFrom,
        dataIndex: 'waypoints',
        key: Columns.AddressFrom,
        render: waypoints => waypoints?.[0] && (waypoints[0].fullAddress ?? getAddress(waypoints[0])),
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.Recipient,
        dataIndex: 'requests',
        key: Columns.Recipient,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {x.recipient?.organization || '-'}
            </div>
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Unload).map(request => (
                <div key={request.humanReadableId}>
                  {request.organization || '-'}
                  <br />
                </div>
              ))
            ))
          ))
        ),
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.RecipientContact,
        dataIndex: 'requests',
        key: Columns.RecipientContact,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {getFullName({ firstName: x.recipient?.firstName ?? '', patronymic: x.recipient?.patronymic ?? '' })}
              <br />
              {x.recipient?.phone ?? x.recipient?.mobilePhone}
            </div>
          ))
        ) : (
          record.waypoints
            .filter(waypoint => waypoint.contacts?.some(contact => (
              contact.requests.some(request => request.type === RequestType.Unload)
            )))
            .map(waypoint => (
              waypoint.contacts?.map(contact => (
                <div key={`${contact.contact.phone}${waypoint.index}`}>
                  {contact.contact.fullName}
                  <br />
                  {contact.contact.phone}
                  <br />
                </div>
              ))
            ))
        ),
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.AddressTo,
        dataIndex: 'waypoints',
        key: Columns.AddressTo,
        render: waypoints => waypoints?.at(-1) && (waypoints?.at(-1).fullAddress ?? getAddress(waypoints.at(-1))),
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
        title: t.Requests.ColumnsCargo.CargoName,
        dataIndex: 'requests',
        key: Columns.CargoName,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <CargoName
              key={x.id}
              request={x}
              requests={requests}
              index={index}
            />
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                <div key={request.humanReadableId}>
                  {request.cargo?.map(cargo => (
                    <div key={cargo.cargoName}>
                      {cargo.cargoName}
                    </div>
                  ))}
                </div>
              ))
            ))
          ))
        ),
        width: 200,
      },
      {
        title: t.Requests.ColumnsCargo.CargoWeight,
        dataIndex: 'requests',
        key: Columns.CargoWeight,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              <div>
                Всего:
                {' '}
                {x.weight}
              </div>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {x.cargoData?.map((cargo, cargoIndex) => (
                <span key={cargo.id}>
                  {cargo.name}
                  :
                  {' '}
                  {cargo.weight}
                  {cargoIndex !== (x.cargoData?.length ?? 0) - 1 && <br />}
                </span>
              ))}
            </div>
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                <div key={request.humanReadableId}>
                  {request.cargo?.map(cargo => (
                    <div key={cargo.cargoName}>
                      {cargo.weight}
                    </div>
                  ))}
                </div>
              ))
            ))
          ))
        ),
        width: 150,
      },
      {
        title: t.Requests.ColumnsCargo.CargoVolume,
        dataIndex: 'requests',
        key: Columns.CargoVolume,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              <div>
                Всего:
                {' '}
                {x.volume && (+(x.volume / CUBE_CM_TO_METERS).toFixed(3) || 0.001)}
              </div>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {x.cargoData?.map((cargo, cargoIndex) => (
                <span key={cargo.id}>
                  {cargo.name}
                  :
                  {' '}
                  {cargo.volume && (+(cargo.volume / CUBE_CM_TO_METERS).toFixed(3) || 0.001)}
                  {cargoIndex !== (x.cargoData?.length ?? 0) - 1 && <br />}
                </span>
              ))}
            </div>
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                <div key={request.humanReadableId}>
                  {request.cargo?.map(cargo => (
                    <div key={cargo.cargoName}>
                      {cargo.volume && (+(cargo.volume / CUBE_CM_TO_METERS).toFixed(3) || 0.001)}
                    </div>
                  ))}
                </div>
              ))
            ))
          ))
        ),
        width: 150,
      },
      {
        title: t.Requests.ColumnsCargo.CommentForDriver,
        dataIndex: 'requests',
        key: Columns.CommentForDriver,
        render: (requests, record) => {
          const _requests = [...requests]
            .reverse();

          if (_requests.length) {
            return _requests.map((request: CargoRequest, index: number) => (
              <div key={request.id}>
                {_requests.length > 1 && (
                  <span>
                    {index + 1}
                    .
                  </span>
                )}
                {' '}
                {request.comment ?? request.commentForDriver ?? EMPTY_CELL_CONTENT}
              </div>
            ));
          }

          const comments = record.waypoints
            .map(waypoint => waypoint.contacts?.map(contact => contact.requests.map(request => request.comment)))
            .flat(2)
            .filter((comment): comment is string => !!comment);

          return <Comment comments={comments} />;
        },
        width: 250,
      },
      {
        title: t.Requests.ColumnsCargo.Places,
        dataIndex: 'requests',
        key: Columns.Places,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <span key={x.id}>
              {requests.length > 1 && <span>{index + 1}</span>}
              {' '}
              {x.occupiedPlacesCount}
            </span>
          ))
        ) : (
          record.waypoints.map(waypoint => (
            waypoint.contacts?.map(contact => (
              contact.requests.filter(x => x.type === RequestType.Load).map(request => (
                <div key={request.humanReadableId}>
                  {request.cargo?.map(cargo => (
                    <div key={cargo.cargoName}>
                      {cargo.cargoName}
                      {': '}
                      {cargo.occupiedPlacesCount}
                    </div>
                  ))}
                </div>
              ))
            ))
          ))
        ),
        width: 150,
      },
      {
        title: t.Requests.ColumnsCargo.VehicleWeight,
        dataIndex: Columns.Capacity,
        render: (capacity?: number) => capacity ?? EMPTY_CELL_CONTENT,
        width: 150,
      },
      {
        title: t.Requests.ColumnsCargo.Loaders,
        dataIndex: 'requests',
        key: Columns.Loaders,
        render: (requests: CargoTrip['requests'], record) => requests.length ? (
          requests.map((x, index) => (
            <div key={x.id}>
              {requests.length > 1 && (
                <span>
                  {index + 1}
                  .
                </span>
              )}
              {' '}
              Погрузка:
              {' '}
              {x.sourceLoaders}
              <br />
              Разгрузка:
              {' '}
              {x.destinationLoaders}
            </div>
          ))
        ) : (
          record.loaders
        ),
        width: 150,
      },
      {
        title: t.Requests.ColumnsCargo.FactDistance,
        dataIndex: Columns.FactDistance,
        width: 100,
        render: (factDistance: number) => factDistance?.toFixed(2) ?? EMPTY_CELL_CONTENT,
      },
      {
        title: t.Requests.ColumnsCargo.DriverWaitingTime,
        dataIndex: Columns.DriverWaitingTime,
        width: 150,
        render: (driverWaitingTime?: number) => driverWaitingTime
          ? Math.round(driverWaitingTime / 1000 / 60)
          : EMPTY_CELL_CONTENT,
      },
      {
        title: t.Requests.ColumnsCargo.LoadersWorkTime,
        dataIndex: Columns.LoadersWorkTime,
        width: 200,
        render: (loadersWorkTime?: number) => loadersWorkTime
          ? Math.round(loadersWorkTime / 1000 / 60)
          : EMPTY_CELL_CONTENT,
      },
      {
        title: t.Requests.ColumnsCargo.Dispatcher,
        dataIndex: Columns.Dispatcher,
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
    ],
    [
      t,
      isLoading,
      field,
      direction,
      showAddresses,
      isLoadingStatus,
      onShowAddress,
      changeStatus,
      openSetDriver,
      assignTrip,
      openEdit,
    ]
  );

  return { cargoColumns };
};
