/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import './override.scss';
// import {IS_REMOTE} from "constants/constants.env";

// if (!IS_REMOTE) {
//   import('antd/dist/antd.css');  // для девелопа! если микро запущен как главный контейнер
// }

import React, { useEffect, useState } from 'react';
import { Button, Form, Rate } from 'antd';
import QRCode from 'react-qr-code';
import { useHistory } from '@sber-sbertransport/mf-core';

import { useGetCarsharingDeepLink } from 'api/requests';

import * as routes from 'constants/constants.routes';
import { DATE_FORMAT } from 'constants/constants.app';
import {
  cancelStatuses, DataTitles, finishedStatuses, TripStatusesEnum
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import RouteMap from 'utils/RouteMap/RouteMap';
import { zoneTime } from 'utils/trips/times';
import { formatRublesWithoutRemainder } from 'utils';

import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';

import { MapComponent } from 'shared/components/Map/MapComponent';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TModal } from 'shared/ui/Modal/Modal';
import ArrowRight from 'shared/components/Images/arrowRight.svg';
import ArrowLeft from 'shared/components/Images/arrow-left.svg';
import Reload from 'shared/components/Images/reload.svg';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import {
  useCurrentTripRequest,
  useTripRequestDetailedView
} from 'shared/hooks/trip';

import { usePersonalTrip } from '../hooks/personal';
import { RateModal } from '../RateModal/RateModal';
import { useCarsharingTrip } from '../hooks/useCarsharingTrip';
import Process from '../../../Evaluation/Constants/Process';
import { chooseCorrectStatusesList } from '../utils';
import { DeclineModal } from '../DeclineModal/DeclineModal';
import { DeclineModalEmpty } from '../DeclineModalEmpty/DeclineModalEmpty';
import { StatusTripRequest } from '../../StatusTripRequest/StatusTripRequest';
import { TransportPicture } from 'modules/EmployeeApp/components/TaxiClasses/Card/components';
import Support from 'shared/components/Images/support.svg';
import { ApprovedByModal } from '../../approvedByModal/ApprovedByModal';
import { TripFraudMarker } from '../TripFraudMarker/TripFraudMarker';

import styles from './styles.module.scss';

const TripRequestContentCarsharing = ({
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
  const [contractor, setContractor] = useState<any>({});
  const onCancelStatus = request.status && cancelStatuses.some(el => el === request.status);
  const onFinishedStatus = request.status && finishedStatuses.some(el => el === request.status);
  const dataTitle = onCancelStatus ? DataTitles.PLANNED_DATA : onFinishedStatus ? DataTitles.ACTUAL_DATA : DataTitles.PLANNED_DATA;
  const [showApprovedByModal, setShowApprovedByModal] = useState(false);

  const [process, setProcess] = useState<Process>(Process.CLOSE);

  const {
    personalRequestIsApproved,
  } = usePersonalTrip({ request, tripFromCoop });

  const {
    isCarsharingTransport,
    carsharingRequestIsApproved,
  } = useCarsharingTrip(request);

  const deepLink = useGetCarsharingDeepLink(request.id);
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

  const handleCarsharingOpen = (e: React.MouseEvent) => {
    setProcess(Process.START);
    e.stopPropagation();
  };

  const handleCarsharingConfirm = () => {
    setProcess(Process.CLOSE);
    return Promise.resolve(200);
  };

  const handleCarsharingClose = () => {
    setProcess(Process.CLOSE);
  };

  const reloadTrip = () => {
    history.push(routes.TRANSPORT_2_0_CREATE);
    geo.addExistingWaypoint(request);
    const purpose = { purposeId: request.purpose.id, isValid: true };
    purposeStore.setPurpose(purpose);
  };

  const handleGoBack = () => {
    tripStore.toggleIsFromDetailedView(true);
    history.goBack();
  };

  const {
    cancelHandler,
  } = useTripRequestDetailedView({
    requestIsApproved: personalRequestIsApproved,
  });

  useEffect(() => {
    request.tariffId && tripStore.getContractorCarsharing(request.tariffId)
      .then(contractor => setContractor(contractor));
  }, []);

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
              onClick={e => { reloadTrip(), e.stopPropagation(); }}
              block
              className={styles.buttonReload}
            >
              <img src={Reload} alt="Reload" />
            </Button>
            {request.isFinished && !request.isRated
            && (
              <Button
                onClick={e => { setShowModal(true), e.stopPropagation(); }}
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

            {isCarsharingTransport && carsharingRequestIsApproved && (
            <Button
              onClick={handleCarsharingOpen}
              block
              className={styles.button}
            >
              Продолжить в мобильном приложении
            </Button>
            )}
            {/* оставлено до уточнения, пока что переводим пользователя в приложение агента, на создание и завершение */}
            {/* {carsharingRequestIsApproved && isCarsharingTransport && (
                <Button onClick={startCarsharingTrip} block className={styles.button}>
                  Начать поездку
                </Button>
              )}
              {(personalTripInProgress || carsharingTripInProgress) && activeStatus?.cancelable && (
                <Button block className={styles.button} htmlType="submit">
                  Завершить поездку
                </Button>
              )} */}
            {activeStatusEntity?.cancelable && currentTripRequest?.transportType === TransportTypeEnum.TAXI && <DeclineModal id={currentTripRequest.id} cancelHandler={cancelHandler} />}
            {activeStatusEntity?.cancelable && currentTripRequest && currentTripRequest?.transportType !== TransportTypeEnum.TAXI && <DeclineModalEmpty cancelHandler={() => cancelHandler('Причина по умолчанию', currentTripRequest.id)} />}
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
                    <span>{formatRublesWithoutRemainder(request?.costInRubles)}</span>
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
      <div className={styles.counterpartyWrapper}>
        <span className={styles.counterpartyTitle}>Контрагент</span>
        <div className={styles.counterpartyInfo}>
          <div className={styles.transportPicture}>
            {request.transportType && <TransportPicture transportType={request.transportType} />}
          </div>
          <span>{contractor && contractor.name}</span>
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

      <TModal
        properties={{
          title: 'Отсканируйте QR-код',
          confirmButton: { text: 'Готово' },
          declineButton: { text: 'Отменить', hide: true },
          handleConfirm: handleCarsharingConfirm,
          style: {
            width: 300,
            height: 511,
            justifyContent: 'center',
            textAlign: 'center',
            margin: '30px 0 0 0',
            padding: '0 24px 24px 24px',
          },
          withoutScrolls: true,
        }}
        process={process}
        onClose={handleCarsharingClose}
        isPoppup={false}
        isReadOnly={false}
      >
        <div className={styles.modalContainer}>
          <p className={styles.modalContainer__title}>
            Для начала поездки необходимо отсканировать QR-код на вашем смартфоне
          </p>
          <QRCode style={{ width: '100%' }} value={deepLink.data.deepLink} />
        </div>
      </TModal>
      <div onClick={e => e.stopPropagation()}>
        <ApprovedByModal
          onCancel={onCancelShowApprovedByModal}
          visible={showApprovedByModal}
          delegates={delegatesList}
          supervisor={supervisor}
          request={request}
          checkAwaitingApproval={request.status === TripStatusesEnum.CARSHARING_AWAITING_APPROVAL}
        />
      </div>
    </Form>
  );
};

export default TripRequestContentCarsharing;
