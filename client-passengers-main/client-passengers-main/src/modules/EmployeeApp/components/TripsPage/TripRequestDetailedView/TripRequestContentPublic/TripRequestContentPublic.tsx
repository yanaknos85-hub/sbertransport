/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import './override.scss';

import React, { useEffect, useState } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import {
  Button, Form, Rate
} from 'antd';
import { UUID } from 'utils/io-ts';

import * as routes from 'constants/constants.routes';
import { DATE_FORMAT } from 'constants/constants.app';
import {
  cancelStatuses, DataTitles, finishedStatuses, TripStatusesEnum
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { zoneTime } from 'utils/trips/times';
import { formatRublesWithoutRemainder, formatRublesfromRubles } from 'utils/MoneyUtils';
import RouteMap from 'utils/RouteMap/RouteMap';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripRequestModel } from 'stores/Trip/models/TripRequest.model';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';

import { MapComponent } from 'shared/components/Map/MapComponent';
import ArrowRight from 'shared/components/Images/arrowRight.svg';
import ArrowLeft from 'shared/components/Images/arrow-left.svg';
import Reload from 'shared/components/Images/reload.svg';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import {
  useCurrentTripRequest,
  useTripRequestDetailedView
} from 'shared/hooks/trip';

import { usePersonalTrip } from '../hooks/personal';
import { RateModal } from '../RateModal/RateModal';
import { chooseCorrectStatusesList } from '../utils';
import { DeclineModalEmpty } from '../DeclineModalEmpty/DeclineModalEmpty';
import { StatusTripRequest } from '../../StatusTripRequest/StatusTripRequest';
import CompensationInfoImage from './Compensation/CompensationInfoImage';
import Support from 'shared/components/Images/support.svg';
import { ApprovedByModal } from '../../approvedByModal/ApprovedByModal';
import { TripFraudMarker } from '../TripFraudMarker/TripFraudMarker';

import styles from './styles.module.scss';

const TripRequestContentPublic = ({
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
  const [showModal, setShowModal] = useState(false);
  const [relatedApplication, setRelatedApplication] = useState<TripRequestModel | undefined>();
  const [showApprovedByModal, setShowApprovedByModal] = useState(false);
  const onCancelStatus = request.status && cancelStatuses.some(el => el === request.status);
  const onFinishedStatus = request.status && finishedStatuses.some(el => el === request.status);
  const dataTitle = onCancelStatus ? DataTitles.PLANNED_DATA : onFinishedStatus ? DataTitles.ACTUAL_DATA : DataTitles.PLANNED_DATA;

  const {
    personalRequestIsApproved,
  } = usePersonalTrip({ request, tripFromCoop });

  const [form] = Form.useForm();

  const tabProps = useTabsNavProps();
  const { activeTab } = tabProps;

  const { currentTripRequest } = useCurrentTripRequest();

  const [, setAvatar] = useState<string>('');

  useEffect(() => {
    if (request.payRequestIds?.length) {
      tripStore.getTripRequestForTransport(request.payRequestIds[0], TransportTypeEnum.PERSONAL)
        .then(el => setRelatedApplication(el));
    }
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
    geo.addExistingWaypoint(request);
    const purpose = { purposeId: request.purpose.id, isValid: true };
    purposeStore.setPurpose(purpose);
  };

  const {
    cancelHandler,
  } = useTripRequestDetailedView({
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
            {declineCondition && currentTripRequest && <DeclineModalEmpty cancelHandler={() => cancelHandler('Причина по умолчанию', currentTripRequest.id)} />}
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
              {request.coopTrip ? 'Совместная поездка' : 'Индивидуальная поездка' }
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
                <div className={styles.wrapperTaxesInfo}>До вычета НДФЛ</div>
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
      <div className={styles.ticketsWrapper}>
        <span className={styles.ticketsTitle}>
          Билеты
          <span className={styles.informationTicketsCount}>{request.transportCompensation?.length}</span>
        </span>
        <div className={styles.ticketsItemsWrapper}>
          {request.transportCompensation?.map(el => (
            <div className={styles.ticketsItemWrapper}>
              <div className={styles.ticketsItem}>
                <span className={styles.ticketsItemTitle}>
                  {el.compensationType?.name === TransportCompensations.CITY_TRIP_COMPENSATION
                    ? 'Разовая поездка'
                    : el.compensationType?.name === TransportCompensations.SUBURB_TRIP_COMPENSATION
                      ? 'Междугородняя поездка'
                      : el.compensationType?.name === TransportCompensations.PAID_SERVICES_COMPENSATION
                        ? 'Платные сервисы' : 'Проездной документ'}
                </span>
                <div className={styles.ticketsItemInfo}>
                  <div className={styles.ticketsItemInfoTransportTypeWrapper}>
                    {el.transportType && <div className={styles[el.transportType?.name]} />}
                    <div className={styles.ticketsItemMainInfo}>
                      <span className={styles.ticketsItemInfoTransportType}>{`${el.transportType?.rusName} x ${el.ticketsCount}`}</span>
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
                      <span>{formatRublesfromRubles((Number(el.ticketsCost) * Number(el.ticketsCount)) / 100)}</span>
                    </div>
                  </div>
                </div>
              </div>
              {el.attachedDocumentId && (
              <CompensationInfoImage
                document={{
                  id: el.attachedDocumentId as UUID,
                  fileFormat: el.compensationDocumentDTO?.fileFormat,
                  fileName: el.compensationDocumentDTO?.fileName,
                }}
              />
              )}
            </div>
          )
          )}
        </div>
      </div>
      <div className={styles.routTripRequest}>
        <div className={styles.commentForDriver}>
          <span>Комментарий к цели поездки</span>
          <span>{request.commentForPurpose}</span>
        </div>
      </div>

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
          checkAwaitingApproval={request.status === TripStatusesEnum.PUBLIC_AWAITING_APPROVAL}
        />
      </div>
    </Form>
  );
};

export default TripRequestContentPublic;
