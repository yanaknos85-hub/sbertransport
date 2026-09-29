/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import './override.scss';
// import {IS_REMOTE} from "constants/constants.env";

// if (!IS_REMOTE) {
//   import('antd/dist/antd.css');  // для девелопа! если микро запущен как главный контейнер
// }

import React, { useEffect, useState } from 'react';
import { Button, Form, Rate } from 'antd';
import classNames from 'classnames';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Employee } from '@sber-sbertransport/mf-core';

import * as routes from 'constants/constants.routes';
import { DATE_FORMAT } from 'constants/constants.app';
import {
  DataTitles,
  TripStatusesEnum,
  TTripRequestStatuses,
  TripStatusesFinal,
  cancelStatuses,
  finishedStatuses
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { useCompleteTripRequest } from 'api/trip-requests';
import { useCarActualInfo } from 'api';

import { UUID } from 'utils/io-ts';
import RouteMapListCheckIn from 'utils/RouteMapListCheckIn/RouteMapListCheckIn';
import { parseNumber } from 'utils/parseNumber';
import { formatRubles, formatRublesWithoutRemainder, getTimeString } from 'utils';
import { zoneTime } from 'utils/trips/times';
import RouteMap from 'utils/RouteMap/RouteMap';
import { getFullEmployeeName } from 'utils/formatFullName';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';
import { IRequestsSharedModal, busClasses } from 'stores/Trip/Trip.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';

import { MapComponent } from 'shared/components/Map/MapComponent';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { WaypointField } from 'shared/models/geo/types';
import User from 'shared/components/Images/user.png';
import ArrowRight from 'shared/components/Images/arrowRight.svg';
import ArrowLeft from 'shared/components/Images/arrow-left.svg';
import Reload from 'shared/components/Images/reload.svg';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import {
  useCurrentTripRequest,
  useTripRequestDetailedView
} from 'shared/hooks/trip';

import { AddingFileComponent } from './AddingFileComponent';
import { useFormAbsenceReason } from './hooks/absence';
import { usePersonalTrip } from './hooks/personal';
import { RateModal } from './RateModal/RateModal';
import { useDescriptionData } from './utils';
import { getWaypointFields } from './WayPoints/utils';
import { useCarsharingTrip } from './hooks/useCarsharingTrip';
import { chooseCorrectStatusesList } from './utils';
import { DeclineModalEmpty } from './DeclineModalEmpty/DeclineModalEmpty';
import PassengersItem from './PassengersItem/passengersItem';
import { StatusTripRequest } from '../StatusTripRequest/StatusTripRequest';
import JoinedPassengersItem from './JoinedPassengers/JoinedPassengersItem';
import { TripFraudMarker } from './TripFraudMarker/TripFraudMarker';
import CompensationInfoImage from './TripRequestContentPublic/Compensation/CompensationInfoImage';
import ModalDeclineCostRetention from './ModalDeclineCostRetention/ModalDeclineCostRetention';
import TaxiMapTimer from './TaxiMapTimer/TaxiMapTimer';
import Support from 'shared/components/Images/support.svg';
import { ApprovedByModal } from '../approvedByModal/ApprovedByModal';

import styles from './styles.module.scss';

const taxiMapPointStatus = ['TAXI_DRIVER_FOUND', 'TAXI_DRIVER_ON_THE_WAY', 'TAXI_DRIVER_ARRIVED'];

const TripRequestContent = ({
  request,
  delegatesList,
  tripFromCoop,
  supervisor,
}: {
  request: TripRequestModel;
  delegatesList: Delegate[];
  tripFromCoop?: TripFromCoopModel;
  supervisor?: IDepartmentHead;
}): JSX.Element => {
  const tripRequestRoute = useTripRequestRoute(request);
  const { actualRoute } = tripRequestRoute;
  const history = useHistory();
  const delegatesNamesList: string[] = delegatesList?.map(delegate => getFullEmployeeName(delegate.delegateEmployee));
  const delegatesNames = delegatesNamesList?.length ? delegatesNamesList : 'Список делегатов пуст';
  const {
    activeStatus,
  } = useDescriptionData({
    request, delegates: delegatesNames, tripFromCoop,
  });
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.purposeStore]: purposeStore,
    [StoreNames.tripStore]: tripStore,
    authStore: { token },
  } = useAppStoreContext();
  const [showModal, setShowModal] = useState(false);
  const [relatedApplication, setRelatedApplication] = useState<TripRequestModel | undefined>();
  const [showApprovedByModal, setShowApprovedByModal] = useState(false);

  const carActualInfo = useCarActualInfo(
    token,
    request.id,
    taxiMapPointStatus.includes(request.status as TTripRequestStatuses)
  );
  const { location: taxiPosition } = carActualInfo ?? {};

  const position = {
    latitude: actualRoute.waypoints[0].latitude,
    longitude: actualRoute.waypoints[0].longitude,
  };

  const actualStatus = carActualInfo?.requestStatus ?? request.status;
  const isCarMonitoring = taxiMapPointStatus.includes(actualStatus as TTripRequestStatuses);
  const isTaxiFinalStatus = !!actualStatus && TripStatusesFinal.includes(actualStatus as TTripRequestStatuses);

  const {
    isPersonalTransport,
    personalTripInProgress,
    personalRequestIsApproved,
    isNotSharedOwner,
    startPersonalTrip,
  } = usePersonalTrip({ request, tripFromCoop });

  const {
    isCarsharingTransport,
    carsharingTripInProgress,
    carsharingRequestIsApproved,
    startCarsharingTrip,
  } = useCarsharingTrip(request);

  const waypointFields: WaypointField[] = getWaypointFields(actualRoute.waypoints);

  useFormAbsenceReason({ request, waypointFields });
  const [completeTripRequest] = useCompleteTripRequest();
  const [form] = Form.useForm();

  const { kpi } = tripFromCoop || {};

  const tabProps = useTabsNavProps();
  const { activeTab } = tabProps;

  const { currentTripRequest } = useCurrentTripRequest();

  const [avatar, setAvatar] = useState<string>('');

  useEffect(() => {
    const userId = request.coopTrip
      ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].employee?.id
      : request.author.userId;

    tripStore.getUserAvatart(userId)
      .then(setAvatar);
    tripStore.setCurrentTripRequest(request.id);
  }, []);

  useEffect(() => {
    return () => tripStore.clearCurrentRequest();
  }, []);

  const statusList = chooseCorrectStatusesList(currentTripRequest?.transportType)().data;
  const activeStatusEntity = (currentTripRequest?.status && statusList.find(status => status.name === currentTripRequest.status));

  const user = request.coopTrip
    ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].employee
    : request?.author;
  const fullName = user && getFullEmployeeName(user);

  const handleFinishTripRequest = async () => {
    if (request?.id) {
      completeTripRequest({ reqId: request?.id }).then(() => {
        goToPlanned();
      });
    }
  };

  const handleGoBack = () => {
    tripStore.toggleIsFromDetailedView(true);
    history.goBack();
  };

  const reloadTrip = () => {
    history.push(routes.TRANSPORT_2_0_CREATE);
    geo.addExistingWaypoint(request);
    const purpose = { purposeId: request.purpose.id, isValid: true };
    purposeStore.setPurpose(purpose);
  };

  const {
    goToPlanned, cancelHandler,
  } = useTripRequestDetailedView({
    requestIsApproved: personalRequestIsApproved,
  });

  useEffect(() => {
    if (request.payRequestIds?.length) {
      tripStore.getTripRequestForTransport(request.payRequestIds[0], TransportTypeEnum.PUBLIC)
        .then(el => setRelatedApplication(el));
    }
  }, []);

  const firstName = request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].employee?.firstName : request.author.firstName;
  const lastName = request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].employee?.lastName : request.author.lastName;
  const patronymic = request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].employee?.patronymic : request.author.patronymic;
  const mobilePhone = tripFromCoop?.requests?.filter(el => el.sharedRideOwner === true)[0].employee?.mobilePhone;
  const registrationNumber = request.transportType === 'PERSONAL' && request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].car?.registrationNumber : request.personalCar?.registrationNumber;
  const model = request.transportType === 'PERSONAL' && request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].car?.model : request.personalCar?.model;
  const brandName = request.transportType === 'PERSONAL' && request.coopTrip ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === true)[0].car?.brandName : request.personalCar?.brandName;
  const passengers: IRequestsSharedModal[] | undefined = request.transportType === 'TAXI'
    ? request.coopTrip
      ? tripFromCoop?.requests.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.TAXI_AWAITING_APPROVAL)
      : []
    : tripFromCoop?.requests.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.PERSONAL_AWAITING_APPROVAL);
  const joinedPassengers: Employee[] | undefined = request.coopTrip
    ? request.joinedPassengers
    && request.transportType === 'PERSONAL'
      ? request.joinedPassengers?.filter(el => el.userId !== request.passenger.userId)
      : request.joinedPassengers
    : request.joinedPassengers?.length
      ? request.joinedPassengers
      : request.transportType === 'PERSONAL' ? [] : [request.passenger];
  const activePassengers = tripFromCoop?.requests.filter(el => el?.status !== TripStatusesEnum.PERSONAL_CANCELLED && el?.status !== TripStatusesEnum.TAXI_CANCELLED);
  const countActivePassengers = request.transportType === 'TAXI'
    ? request.coopTrip
      ? activePassengers?.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.TAXI_AWAITING_APPROVAL).length
      : 0
    : activePassengers?.filter(el => el.sharedRideOwner === false && el.status !== TripStatusesEnum.PERSONAL_AWAITING_APPROVAL).length;
  const countJoinedPassengers = request.coopTrip
    ? request.joinedPassengers
    && request.transportType === 'PERSONAL'
      ? joinedPassengers?.filter(el => el.userId !== request.passenger.userId).length
      : request.joinedPassengers?.length
    : request.joinedPassengers?.length
      ? request.joinedPassengers?.length
      : request.transportType === 'PERSONAL' ? [] : [request.passenger]?.length;
  const manualCheckInStatuses = request.status === TripStatusesEnum.PERSONAL_AWAITING_TRIP_APPROVAL
    || request.status === TripStatusesEnum.PERSONAL_ORDER_PAYMENT_FORMATION
    || request.status === TripStatusesEnum.PERSONAL_PAYMENT_AWAITING;
  const checkingRouteManually = (personalTripInProgress || manualCheckInStatuses || request.isFinal)
    && isNotSharedOwner
    && request.transportType === TransportTypeEnum.PERSONAL
    && request.status
    && !cancelStatuses.includes(request.status);
  const kpiCost = kpi?.ordersKpi[0].orderPriceKop && (Math.round(kpi?.ordersKpi[0].orderPriceKop / 100));
  const cost = request.coopTrip && request.transportType === TransportTypeEnum.PERSONAL
    ? tripFromCoop?.requests.some(el => el.sharedRideOwner === true && el.humanReadableId === request.humanReadableId) && request.expected.cost
      ? (Math.round((request.expected.cost / 100)))
      : kpiCost
    : request.coopTrip
      ? kpiCost
      : request?.costInRubles;
  const onCancelStatus = request.status && cancelStatuses.some(el => el === request.status);
  const onFinishedStatus = request.status && finishedStatuses.some(el => el === request.status);
  const dataTitle = onCancelStatus ? DataTitles.PLANNED_DATA : onFinishedStatus ? DataTitles.ACTUAL_DATA : DataTitles.PLANNED_DATA;
  const waitTime = request.expected.waypoints.reduce((acc, value) => acc + value.waitTime, 0);
  const busClass = busClasses.find(el => el === request.taxiClass);
  const costRetention = (request.status === TripStatusesEnum.TAXI_DRIVER_ARRIVED || request.status === TripStatusesEnum.TAXI_TRIP_IN_PROGRESS);

  const handleRouteTabs = () => {
    history.push(`${routes.TRIPS_LIST}${activeTab === 'final' ? '/final' : '/planned'}`);
  };

  const tabTitle = activeTab === 'final' ? 'Завершённые' : 'Активные';
  const joinedPassengersReverse = joinedPassengers && [...joinedPassengers].reverse();

  const handleShowApprovedByModal = e => {
    setShowApprovedByModal(true);
    e.stopPropagation();
  };

  const onCancelShowApprovedByModal = () => {
    setShowApprovedByModal(false);
  };

  const fraudMessage = request?.fraud?.comment;
  const hasFraudComments = request.fraudComment && request.fraudComment.length !== 0;
  const hasFraud = fraudMessage || hasFraudComments;

  return (
    <Form
      className={styles.wrapperTripRequest}
      onFinish={handleFinishTripRequest}
      form={form}
    >
      <div className={styles.headerTripRequest}>
        <span onClick={() => history.push(`${routes.TRIPS_LIST}/planned`)}>Мои поездки</span>
        <img src={ArrowRight} alt="ArrowRight" />
        <span onClick={handleRouteTabs}>{tabTitle}</span>
        <img src={ArrowRight} alt="ArrowRight" />
        <span>{`Поездка ${request.humanReadableId}`}</span>
      </div>
      <div className={styles.generalInformationTripRequest}>
        {hasFraud && <TripFraudMarker />}
        <div className={styles.wrapperGeneralInformationTripRequest}>
          <div className={styles.headerGeneralInformationTripRequest}>
            <img
              src={ArrowLeft}
              alt="ArrowLeft"
              onClick={handleGoBack}
            />
            <span className={styles.humanReadableId}>{`Поездка ${request.humanReadableId}`}</span>
            <span>{request?.purpose && request.purpose.label}</span>
          </div>
          <div className={styles.buttons}>
            <Button
              onClick={e => {
                reloadTrip();
                e.stopPropagation();
              }}
              block
              className={styles.buttonReload}
            >
              <img src={Reload} alt="Reload" />
            </Button>
            {request.isFinished && !request.isRated
            && (
            <Button
              onClick={e => {
                setShowModal(true);
                e.stopPropagation();
              }}
              block
              className={styles.button}
            >
              Оценить
            </Button>
            )}
            {request.requestRating?.rating
            && (
            <div className={styles.rate}>
              <Rate disabled defaultValue={request.requestRating?.rating} />
            </div>
            )}
            {request.status === 'PUBLIC_TRIP_CONFIRMATION' && <AddingFileComponent request={request} />}
            {personalRequestIsApproved && isPersonalTransport && isNotSharedOwner && (
              <Button
                onClick={startPersonalTrip}
                block
                className={styles.button}
              >
                Начать поездку
              </Button>
            )}
            {carsharingRequestIsApproved && isCarsharingTransport && (
              <Button
                onClick={startCarsharingTrip}
                block
                className={styles.button}
              >
                Начать поездку
              </Button>
            )}
            {(personalTripInProgress || carsharingTripInProgress) && activeStatus?.cancelable && isNotSharedOwner && (
              <Button
                block
                className={styles.button}
                htmlType="submit"
              >
                Завершить поездку
              </Button>
            )}
            {activeStatusEntity?.cancelable && currentTripRequest && currentTripRequest?.transportType !== TransportTypeEnum.TAXI && <DeclineModalEmpty cancelHandler={() => cancelHandler('Причина по умолчанию', currentTripRequest.id)} />}
            {activeStatusEntity?.cancelable && currentTripRequest?.transportType === TransportTypeEnum.TAXI
            && (
            <ModalDeclineCostRetention
              id={currentTripRequest.id}
              humanReadableId={request.humanReadableId}
              cancelHandler={cancelHandler}
              costRetention={costRetention}
            />
            )}
          </div>
        </div>
        <div className={styles.approvedBy} onClick={handleShowApprovedByModal}>
          <img src={Support} alt="Support" />
          Посмотреть согласующих
        </div>
        <div className={styles.wrapperTransportType}>
          <div className={styles.transportType}>
            <div className={styles.transportTypeWrapper}>
              <div className={styles.TransportPictureWrapper}>
                <div className={classNames(busClass ? TransportTypeEnum.BUS : request.transportType)} />
              </div>
              <span>{request.transportType && TransportTypeTitlesEnum[request.transportType]}</span>
            </div>
            <div className={styles.date}>
              <span>{zoneTime(request?.desiredDate, DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME, request?.timeZone)}</span>
            </div>
            <span className={styles.coopTrip}>
              {request.coopTrip ? 'Совместная поездка' : 'Индивидуальная поездка'}
            </span>
          </div>
        </div>
        <span className={styles.line} />
        <div className={styles.footerGeneralInformationTripRequest}>
          <div>
            <div className={styles.statusLabel}>Статус заявки:</div>
            <StatusTripRequest status={actualStatus as TTripRequestStatuses} type={request?.transportType} />
          </div>
          <div className={styles.wrapperfInformationTrip}>
            <div className={styles.wrapperBriefInformationTrip}>
              <div>
                <div className={styles.wrapperDataTrip}>{dataTitle}</div>
                <div className={styles.briefInformationTrip}>
                  <div className={styles.wrapperInformation}>
                    <span>Время в пути</span>
                    <span>{request?.expected.getTimeString()}</span>
                  </div>
                  {request.transportType === TransportTypeEnum.TAXI && (
                  <div className={styles.wrapperInformation}>
                    <span>Время ожидания</span>
                    <span>{getTimeString(waitTime)}</span>
                  </div>
                  )}
                  <div className={styles.wrapperInformation}>
                    <span>Расстояние</span>
                    <span>{request?.expected.getDistanceString()}</span>
                  </div>
                  <div className={styles.wrapperInformation}>
                    <span>Стоимость услуг</span>
                    <span>{formatRublesWithoutRemainder(cost)}</span>
                  </div>
                </div>
                {(request?.transportType === TransportTypeEnum.PERSONAL
                || request?.transportType === TransportTypeEnum.YANDEX) && (
                  <div className={styles.wrapperTaxesInfo}>До вычета НДФЛ</div>
                )}
              </div>
            </div>
            {request?.additionalSum !== undefined && request?.additionalSum > 0
            && (
            <div className={styles.wrapperCostPerPassenger}>
              <div className={styles.costPerPassenger}>
                {`+${formatRublesWithoutRemainder(request.additionalSum / 100)}`}
              </div>
            </div>
            )}
          </div>
        </div>
      </div>
      <div className={styles.routTripRequest}>
        <div className={styles.rout}>
          <span className={styles.routTitle}>Маршрут</span>
          {checkingRouteManually ? (
            <RouteMapListCheckIn request={request} tripRequestRoute={tripRequestRoute} />
          )
            : (
              <RouteMap addresses={request.expected.waypoints} />
            )}
        </div>
        {(isCarMonitoring || request.transportType === TransportTypeEnum.PERSONAL || isTaxiFinalStatus)
        && (
        <div className={styles.routMap}>
          <MapComponent
            markers={actualRoute.waypoints}
            polylines={!isCarMonitoring ? actualRoute.segments : []}
            className={styles.map}
            dragging
            zoomControl
            carPosition={taxiPosition}
            isCarMonitoring={isCarMonitoring}
            position={position}
            boundsPadding={{
              top: 40, bottom: 40, left: 40, right: 40,
            }}
          />
          {carActualInfo && (
            <TaxiMapTimer status={actualStatus as TripStatusesEnum} duration={carActualInfo.duration} />
          )}
        </div>
        )}
      </div>
      <div className={styles.carTripRequest}>
        <div className={styles.routTripRequest}>
          <div className={styles.informationCar}>
            <span className={styles.informationCarTitle}>Автомобиль</span>
            <div className={styles.informationCarWrapper}>
              <div className={styles.informationCarMain}>
                <div className={styles.TransportPictureWrapper}>
                  <div className={classNames(busClass ? TransportTypeEnum.BUS : request.transportType)} />
                </div>
              </div>
              <div className={styles.informationCarNumber}>
                <span>
                  {request.transportType === 'TAXI' ? request.driverInfo?.registrationNumber : registrationNumber}
                </span>
                <span>
                  {request.transportType === 'TAXI' ? request.driverInfo?.vehicleInfo : `${brandName} ${model}`}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div className={styles.routTripRequest}>
          <div className={styles.informationDriver}>
            <span className={styles.informationDriverTitle}>Водитель</span>
            <div className={styles.informationDriverWrapper}>
              <div className={styles.informationDriverMain}>
                <div className={styles.TransportPictureWrapper}>
                  <img alt="avatar" src={request.transportType === 'TAXI' ? User : avatar ? avatar : User} />
                </div>
              </div>
              <div className={styles.informationDriverNumber}>
                <span>
                  {request.transportType === 'TAXI' ? request.driverInfo?.driverName : `${lastName} ${firstName} ${patronymic}`}
                </span>
                <span>
                  {request.transportType === 'TAXI' ? request.driverInfo?.driverPhone && parseNumber(request.driverInfo?.driverPhone) : mobilePhone && parseNumber(mobilePhone)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      {request.transportType === 'TAXI'
      && (
      <div className={styles.routTripRequest}>
        <div className={styles.informationinInitiator}>
          <span className={styles.informationinInitiatorTitle}>
            Инициатор
          </span>
          <div className={styles.informationinInitiatorWrapper}>
            <div className={styles.informationinInitiatorMain}>
              <img alt="avatar" src={avatar ? avatar : User} />
              <div className={styles.informationinInitiatorInfo}>
                <span className={styles.informationinInitiatorInfoName}>
                  {fullName}
                </span>
                {/* <span className={styles.informationinInitiatorInfoPosition}>
                    {request.author.positionName}
                  </span> */}
                {/* <span className={styles.informationinInitiatorInfoNumber}>
                    {request.author.personnelNumber}
                  </span> */}
              </div>
            </div>
          </div>
        </div>
      </div>
      )}
      {(request.coopTrip || request.transportType === 'TAXI') && (
      <div className={styles.routTripRequest}>
        <div className={styles.informationPassengers}>
          <span className={styles.informationPassengersTitle}>
            Пассажиры со мной
            <span className={styles.informationPassengersCount}>{countJoinedPassengers}</span>
          </span>
          <div className={styles.informationPassengersList}>
            {joinedPassengersReverse?.map((item, index) => (
              <JoinedPassengersItem key={index} item={item} />
            ))}
          </div>
        </div>
      </div>
      )}
      {request.coopTrip && (
      <div className={styles.routTripRequest}>
        <div className={styles.informationPassengers}>
          <span className={styles.informationPassengersTitle}>
            Присоединившиеся пассажиры
            <span className={styles.informationPassengersCount}>{countActivePassengers}</span>
          </span>
          <div className={styles.informationPassengersList}>
            {passengers?.map((item, index) => (
              <PassengersItem key={index} item={item} />
            ))}
          </div>
        </div>
      </div>
      )}
      {relatedApplication
      && (
      <div className={styles.ticketsWrapper}>
        <span className={styles.ticketsTitle}>
          Платные сервисы
          <span className={styles.informationTicketsCount}>{relatedApplication.transportCompensation?.length}</span>
        </span>
        <div className={styles.ticketsItemsWrapper}>
          {relatedApplication.transportCompensation?.map(item => (
            <div className={styles.ticketsItemWrapper}>
              <div className={styles.ticketsItem}>
                <span className={styles.ticketsItemTitle}>
                  {item.transportType?.rusName}
                </span>
                <div className={styles.ticketsItemInfo}>
                  <div className={styles.ticketsItemInfoTransportTypeWrapper}>
                    {item.transportType && <div className={styles[item.transportType?.name]} />}
                    <div className={styles.ticketsItemMainInfo}>
                      {relatedApplication && request.payRequestIds
                      && (
                      <a href={`${routes.TRIPS_LIST_PLANNED}/${request.payRequestIds[0]}`}>
                        {relatedApplication?.humanReadableId}
                      </a>
                      )}
                    </div>
                  </div>
                  <div className={styles.ticketsItemCostWrapper}>
                    <div className={styles.ticketsItemLine} />
                    <div className={styles.ticketsItemCost}>
                      <span>Стоимость</span>
                      <span>{formatRubles((Number(item.ticketsCost) * Number(item.ticketsCount)) / 100)}</span>
                    </div>
                  </div>
                </div>
              </div>
              {item.attachedDocumentId && <CompensationInfoImage document={{ id: item.attachedDocumentId as UUID }} />}
            </div>
          )
          )}
        </div>
      </div>
      )}
      {!request.coopTrip ? (
        <div className={styles.routTripRequest}>
          <div className={styles.commentForDriver}>
            <span>Комментарий к цели поездки</span>
            <span>{request.commentForPurpose}</span>
          </div>
        </div>
      )
        : (
          <div className={styles.routTripRequest}>
            <div className={styles.commentForDriver}>
              <span>Комментарии</span>

              <div className={styles.comments}>
                <div className={styles.mainCommentForPurpose}>
                  <span>
                    К цели поездки:
                  </span>
                  <span>
                    {request.commentForPurpose}
                  </span>
                </div>
                <div className={styles.mainCommentForDriver}>
                  <span>
                    Водителю:
                  </span>
                  <span>
                    {request.commentForDriver}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

      {request.isFinished && !request.isRated && (
        <RateModal
          request={request}
          setShowModal={setShowModal}
          show={showModal}
        />
      )}
      <div onClick={e => e.stopPropagation()}>
        <ApprovedByModal
          onCancel={onCancelShowApprovedByModal}
          visible={showApprovedByModal}
          delegates={delegatesList}
          supervisor={supervisor}
          request={request}
          checkAwaitingApproval={request.status === TripStatusesEnum.PERSONAL_AWAITING_APPROVAL || request.status === TripStatusesEnum.TAXI_AWAITING_APPROVAL}
        />
      </div>
    </Form>
  );
};

export default TripRequestContent;
