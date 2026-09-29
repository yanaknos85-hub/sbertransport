/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import './override.scss';

import React, { useEffect, useState } from 'react';
import { Button, Form, Rate } from 'antd';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Moment } from 'moment';

import * as routes from 'constants/constants.routes';
import { DATE_FORMAT } from 'constants/constants.app';
import {
  cancelStatuses, DataTitles, finishedStatuses, TripStatusesEnum
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import RouteMap from 'utils/RouteMap/RouteMap';
import { zoneTime } from 'utils/trips/times';
import { formatRublesWithoutRemainder } from 'utils';
import { parseNumber } from 'utils/parseNumber';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import {
  useCurrentTripRequest,
  useTripRequestDetailedView
} from 'shared/hooks/trip';
import { MapComponent } from 'shared/components/Map/MapComponent';
import User from 'shared/components/Images/user.png';
import ArrowRight from 'shared/components/Images/arrowRight.svg';
import ArrowLeft from 'shared/components/Images/arrow-left.svg';
import Reload from 'shared/components/Images/reload.svg';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';

import { chooseCorrectStatusesList } from '../utils';
import { declOfNum } from '../utils/declOfNum';
import { usePersonalTrip } from '../hooks/personal';
import { DeclineModalEmpty } from './components/DeclineModalEmpty/DeclineModalEmpty';
import { StatusTripRequest } from '../../StatusTripRequest/StatusTripRequest';
import { RepeatRouteAndAtriumModal } from './components/repeatRouteAndAtriumModal/RepeatAtriumModal';
import { ReloadTripModal } from './components/reloadTripModal/ReloadTripModal';
import Support from 'shared/components/Images/support.svg';
import { ApprovedByModal } from '../../approvedByModal/ApprovedByModal';
import { TripFraudMarker } from '../TripFraudMarker/TripFraudMarker';

import styles from './styles.module.scss';

const TripRequestContentGroupTransfer = ({
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
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.purposeStore]: purposeStore,
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();
  const [visibleReloadTripModal, setVisibleReloadTripModal] = useState(false);
  const [visibleRepeatRouteAndAtriumModal, setVisibleRepeatRouteAndAtriumModal] = useState(false);
  const [choosingTime, setChoosingTime] = useState<Moment | null>(null);
  const [showApprovedByModal, setShowApprovedByModal] = useState(false);

  const onCancelReloadTripModal = () => {
    setVisibleReloadTripModal(false);
  };

  const onCancelRepeatRouteAndAtriumModal = () => {
    setVisibleRepeatRouteAndAtriumModal(false);
  };

  const {
    personalRequestIsApproved,
  } = usePersonalTrip({ request, tripFromCoop });

  const [form] = Form.useForm();

  const tabProps = useTabsNavProps();
  const { activeTab } = tabProps;

  const { currentTripRequest } = useCurrentTripRequest();

  const [, setAvatar] = useState<string>('');

  useEffect(() => {
    tripStore.getUserAvatart(request.author.userId)
      .then(setAvatar);
    tripStore.setCurrentTripRequest(request.id);
  }, []);

  useEffect(() => {
    return () => tripStore.clearCurrentRequest();
  }, []);

  const statusList = chooseCorrectStatusesList(currentTripRequest?.transportType)().data;
  const activeStatusEntity = (currentTripRequest?.status && statusList.find(status => status.name === currentTripRequest.status));

  const reloadTrip = () => {
    history.push(routes.TRANSPORT_2_0_CREATE);
    geo.addExistingWaypoint(request)
      .catch(() => tripStore.setGroupTransferLoadWaypoint(true));
    const purpose = { purposeId: request.purpose.id, isValid: true };
    purposeStore.setPurpose(purpose);
  };

  const reloadRouteAndAtriumTrip = () => {
    reloadTrip();
    tripStore.setGroupTransferInformation({
      information: { ...request.information, passengerCount: request.passengerCount },
      choosingTime: choosingTime,
      commentForPurpose: request.commentForPurpose,
    });
  };

  const confirmReloadTrip = () => {
    if (cancelStatuses.some(el => el === request.status)) {
      return setVisibleReloadTripModal(true);
    }

    reloadTrip();
  };

  const { cancelHandler, justCancelHandler } = useTripRequestDetailedView({
    requestIsApproved: personalRequestIsApproved,
  });

  const handleGoBack = () => {
    tripStore.toggleIsFromDetailedView(true);
    history.goBack();
  };

  const handleShowApprovedByModal = e => {
    setShowApprovedByModal(true);
    e.stopPropagation();
  };

  const onCancelShowApprovedByModal = () => {
    setShowApprovedByModal(false);
  };

  const declineCondition = activeStatusEntity?.cancelable && currentTripRequest?.transportType !== TransportTypeEnum.TAXI;

  // const animal = request.information?.animal; // возможно нужно будет при изменении дизайна
  const animalComment = request.information?.animalComment;
  // const bugs = request.information?.bugs; // возможно нужно будет при изменении дизайна
  // const bugsComment = request.information?.bugsComment;  // возможно нужно будет при изменении дизайна
  // const bugsOversized = request.information?.bugsOversized; // возможно нужно будет при изменении дизайна
  const bugsOversizedComment = request.information?.bugsOversizedComment;
  const childSeatDetails = request.information?.childSeatDetails;
  const clientFullName = request.information?.addContactFIO;
  const clientInfoPhone = request.information?.addContactPhone;
  const dateFlight = request.information?.dateFlight;
  const numberFlight = request.information?.numberFlight;
  const phoneHotel = request.information?.phoneHotel;
  const bugsComment = request.information?.bugsComment;
  const typeVehicle = request.information?.typeVehicle;
  const passengerCount = request.passengerCount;
  const passengerName = request.passenger.fullName;
  const passengerMobilePhone = request.passenger.mobilePhone;
  const onCancelStatus = request.status && cancelStatuses.some(el => el === request.status);
  const onFinishedStatus = request.status && finishedStatuses.some(el => el === request.status);
  const dataTitle = onCancelStatus ? DataTitles.PLANNED_DATA : onFinishedStatus ? DataTitles.ACTUAL_DATA : DataTitles.PLANNED_DATA;
  const fraudMessage = request?.fraud?.comment;
  const hasFraudComments = request.fraudComment && request.fraudComment.length !== 0;
  const hasFraud = fraudMessage || hasFraudComments;

  return (
    <Form className={styles.wrapperTripRequest} form={form}>
      <div className={styles.headerTripRequest}>
        <span onClick={() => history.push(`${routes.TRIPS_LIST}/planned`)}>Мои поездки</span>
        <img src={ArrowRight} alt="ArrowRight" />
        <span onClick={() => history.push(`${routes.TRIPS_LIST}${activeTab === 'final' ? '/final' : '/planned'}`)}>{activeTab === 'final' ? 'Завершённые' : 'Активные'}</span>
        <img src={ArrowRight} alt="ArrowRight" />
        <span>{`Поездка ${request.humanReadableId}`}</span>
      </div>
      <div className={styles.generalInformationTripRequest}>
        {hasFraud && <TripFraudMarker />}
        <div className={styles.wrapperGeneralInformationTripRequest}>
          <div className={styles.headerGeneralInformationTripRequest}>
            <img
              src={ArrowLeft}
              alt="ArrowRight"
              onClick={handleGoBack}
            />
            <span className={styles.humanReadableId}>{`Поездка ${request.humanReadableId}`}</span>
            <span>{request?.purpose && request.purpose.label}</span>
          </div>
          <div className={styles.buttons}>
            <Button
              onClick={confirmReloadTrip}
              block
              className={styles.buttonReload}
            >
              <img src={Reload} alt="Reload" />
            </Button>
            {/* ПОКА НЕ РЕАЛИЗОВАНА ОЦЕНКА ДЛЯ ТРАНСФЕРА НА БЭКЕ
            {request.isFinished && !request.isRated
            && (
            <Button
              onClick={e => { setShowModal(true), e.stopPropagation(); }}
              block
              className={styles.button}
            >
              Оценить
            </Button>
            )} */}
            {request.requestRating?.rating
            && (
            <div className={styles.rate}>
              <Rate disabled defaultValue={request.requestRating?.rating} />
            </div>
            )}
            {declineCondition && currentTripRequest && (
              <DeclineModalEmpty
                cancelHandler={() => cancelHandler('Причина по умолчанию', currentTripRequest.id)}
                cancelAndRepeatHandler={() => {
                  justCancelHandler('Причина по умолчанию', currentTripRequest.id);
                  reloadRouteAndAtriumTrip();
                }}
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
                <div className={request.transportType} />
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
            <StatusTripRequest status={request?.status} type={request?.transportType} />
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
                  <div className={styles.wrapperInformation}>
                    <span>Расстояние</span>
                    <span>{request?.expected.getDistanceString()}</span>
                  </div>
                  <div className={styles.wrapperInformation}>
                    <span>Стоимость услуг</span>
                    <span>{formatRublesWithoutRemainder(request.costInRubles && request?.costInRubles)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.routTripRequest}>
        <div className={styles.rout}>
          <span className={styles.routTitle}>Маршрут</span>
          <RouteMap addresses={request.expected.waypoints} />
        </div>
        <div className={styles.routMap}>
          <MapComponent
            markers={actualRoute.waypoints}
            polylines={actualRoute.segments}
            className={styles.map}
            dragging
            zoomControl
          />
        </div>
      </div>
      <div className={styles.carTripRequest}>
        <div className={styles.routTripRequest}>
          <div className={styles.informationCar}>
            <span className={styles.informationCarTitle}>Автомобиль</span>
            <div className={styles.informationCarWrapper}>
              <div className={styles.informationCarMain}>
                <div className={styles.TransportPictureWrapper}>
                  <div className={request.transportType} />
                </div>
              </div>
              <div className={styles.informationCarNumber}>
                <span>
                  {request.driverInfo?.registrationNumber}
                </span>
                <span>
                  {request.driverInfo?.vehicleInfo}
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
                  <img src={User} alt="User" />
                </div>
              </div>
              <div className={styles.informationDriverNumber}>
                <span>
                  {request.driverInfo?.driverName}
                </span>
                <span>
                  {request.driverInfo?.driverPhone && parseNumber(request.driverInfo?.driverPhone)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.wrapperinfoTrip}>
        <span className={styles.infoTripTitle}>Информация о поездке</span>
        <div className={styles.tripTitleItemsWrapper}>
          {passengerCount
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Количество пассажиров</span>
            <span className={styles.textInfo}>{declOfNum(passengerCount)}</span>
          </div>
          )}
          {passengerName
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>ФИО клиента</span>
            <span className={styles.textInfo}>{passengerName}</span>
          </div>
          )}
          {passengerMobilePhone
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Телефон клиента</span>
            <span className={styles.textInfo}>{parseNumber(passengerMobilePhone)}</span>
          </div>
          )}
          {clientFullName
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>ФИО доп. контактного лица</span>
            <span className={styles.textInfo}>{clientFullName}</span>
          </div>
          )}
          {clientInfoPhone
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Телефон доп. контактного лица</span>
            <span className={styles.textInfo}>{parseNumber(clientInfoPhone)}</span>
          </div>
          )}
          {numberFlight
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Номер рейса/поезда</span>
            <span className={styles.textInfo}>{numberFlight}</span>
          </div>
          )}
          {dateFlight
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Дата и время рейса/поезда</span>
            <span className={styles.textInfo}>{zoneTime(dateFlight, DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME, request?.timeZone)}</span>
          </div>
          )}
          {phoneHotel
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Номер в гостинице</span>
            <span className={styles.textInfo}>{phoneHotel}</span>
          </div>
          )}
          {bugsComment
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Количество багажа</span>
            <span className={styles.textInfo}>{bugsComment}</span>
          </div>
          )}
        </div>
      </div>
      <div className={styles.wrapperinfoTrip}>
        <span className={styles.infoTripTitle}>Дополнительные параметры</span>
        <div className={styles.tripTitleItemsWrapper}>
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Детское кресло</span>
            {childSeatDetails && childSeatDetails.group1 > 0
            && <span className={styles.textInfo}>{`Кресло от 9 мес. до 4 лет - ${childSeatDetails?.group1} шт.`}</span>}
            {childSeatDetails && childSeatDetails.group2 > 0
            && <span className={styles.textInfo}>{`Кресло 3-7 лет - ${childSeatDetails?.group2} шт.`}</span>}
            {childSeatDetails && childSeatDetails.booster > 0
            && <span className={styles.textInfo}>{`бустер 6-12 лет - ${childSeatDetails?.booster} шт.`}</span>}
            {childSeatDetails && childSeatDetails.newborn > 0
            && <span className={styles.textInfo}>{`люлька до 1 года - ${childSeatDetails?.newborn} шт.`}</span>}
          </div>
          {bugsOversizedComment
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Негабаритный багаж</span>
            <span className={styles.textInfo}>{bugsOversizedComment}</span>
          </div>
          )}
          {animalComment
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Перевозка животного</span>
            <span className={styles.textInfo}>{animalComment}</span>
          </div>
          )}
          {typeVehicle
          && (
          <div className={styles.tripTitleItemWrapper}>
            <span className={styles.titleInformation}>Желаемый тип ТС</span>
            <span className={styles.textInfo}>{typeVehicle}</span>
          </div>
          )}
        </div>
      </div>
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

      <ReloadTripModal
        visible={visibleReloadTripModal}
        onCancel={onCancelReloadTripModal}
        reloadTrip={reloadTrip}
        setVisibleRepeatRouteAndAtriumModal={setVisibleRepeatRouteAndAtriumModal}
      />

      <RepeatRouteAndAtriumModal
        visible={visibleRepeatRouteAndAtriumModal}
        onCancel={onCancelRepeatRouteAndAtriumModal}
        reloadTrip={reloadRouteAndAtriumTrip}
        request={request}
        setChoosingTime={setChoosingTime}
        choosingTime={choosingTime}
      />

      <div onClick={e => e.stopPropagation()}>
        <ApprovedByModal
          onCancel={onCancelShowApprovedByModal}
          visible={showApprovedByModal}
          delegates={delegatesList}
          supervisor={supervisor}
          request={request}
          checkAwaitingApproval={request.status === TripStatusesEnum.GROUP_TRANSFER_AWAITING_APPROVAL}
        />
      </div>

      {/* ПОКА НЕ РЕАЛИЗОВАНА ОЦЕНКА ДЛЯ ТРАНСФЕРА НА БЭКЕ
      {request.isFinished && !request.isRated && (
      <RateModal
        request={request}
        setShowModal={setShowModal}
        show={showModal}
      />
      )} */}
    </Form>
  );
};

export default TripRequestContentGroupTransfer;
