import { List, Button, Rate } from 'antd';
import React, { useState } from 'react';
import { Moment } from 'moment';
import classNames from 'classnames';

import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { cancelStatuses, TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';
import * as routes from 'constants/constants.routes';

import { TripRequestModel } from 'stores/Trip/models';

import { formatRublesWithoutRemainder } from 'utils';

import {
  useTripRequestDetailedView
} from 'shared/hooks/trip';

import { StatusTripRequest } from 'modules/EmployeeApp/components/TripsPage/StatusTripRequest/StatusTripRequest';
import { zoneTime } from 'utils/trips/times';
import RouteMap from 'utils/RouteMap/RouteMap';
import { chooseCorrectStatusesList } from '../TripRequestDetailedView/utils';
import { DeclineModal } from '../TripRequestDetailedView/DeclineModal/DeclineModal';
import { DeclineModalEmpty } from '../TripRequestDetailedView/DeclineModalEmpty/DeclineModalEmpty';
import { DeclineModalEmpty as DeclineTransferModalEmpty } from '../TripRequestDetailedView/TripRequestContentGroupTransfer/components/DeclineModalEmpty/DeclineModalEmpty';
import { usePersonalTrip } from '../TripRequestDetailedView/hooks/personal';
import { useCarsharingTrip } from '../TripRequestDetailedView/hooks/useCarsharingTrip';
import Reload from '../../../../../shared/components/Images/reload.svg';
import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { DATE_FORMAT } from 'constants/constants.app';
import { RateModal } from '../TripRequestDetailedView/RateModal/RateModal';
import { busClasses } from 'stores/Trip/Trip.interface';
import { ReloadTripModal } from '../TripRequestDetailedView/TripRequestContentGroupTransfer/components/reloadTripModal/ReloadTripModal';
import { RepeatRouteAndAtriumModal } from '../TripRequestDetailedView/TripRequestContentGroupTransfer/components/repeatRouteAndAtriumModal/RepeatAtriumModal';
import Support from 'shared/components/Images/support.svg';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';
import { ApprovedByModal } from '../approvedByModal/ApprovedByModal';
import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';

import styles from './item.module.scss';
import './item.scss';

interface Props {
  request: TripRequestModel;
  onClickHandler: (id: string) => void;
  isFinished?: boolean;
  delegates: Delegate[];
  supervisor?: IDepartmentHead;
}

export const RenderTripRequestList = ({
  request,
  onClickHandler,
  delegates,
  supervisor,
}: Props): JSX.Element => {
  const statusList = chooseCorrectStatusesList(request?.transportType)().data;
  const activeStatusEntity = (request?.status && statusList.find(status => status.name === request.status));
  const [showModal, setShowModal] = useState(false);
  const [showApprovedByModal, setShowApprovedByModal] = useState(false);
  const [visibleReloadTripModal, setVisibleReloadTripModal] = useState(false);
  const [visibleRepeatRouteAndAtriumModal, setVisibleRepeatRouteAndAtriumModal] = useState(false);
  const [choosingTime, setChoosingTime] = useState<Moment | null>(null);
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.geoStore]: geo,
    [StoreNames.purposeStore]: purposeStore,
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  const TAXES = request?.transportType === TransportTypeEnum.PERSONAL
    || request?.transportType === TransportTypeEnum.PUBLIC
    || request?.transportType === TransportTypeEnum.YANDEX;

  const { history } = configStore;

  const {
    personalTripInProgress,
    personalRequestIsApproved,
  } = usePersonalTrip({ request });

  const {
    carsharingTripInProgress,
  } = useCarsharingTrip(request);

  const { cancelHandler, justCancelHandler } = useTripRequestDetailedView({
    requestIsApproved: personalRequestIsApproved,
  });

  const reloadTrip = () => {
    history.push(routes.TRANSPORT_2_0_CREATE);
    geo.addExistingWaypoint(request)
      .catch(() => request.transportType === TransportTypeEnum.GROUP_TRANSFER && tripStore.setGroupTransferLoadWaypoint(true));
    const purpose = { purposeId: request.purpose.id, isValid: true };
    purposeStore.setPurpose(purpose);
  };

  const handleAssess = () => {
    setShowModal(true);
    tripStore.setCurrentTripRequest(request.id);
  };

  const confirmReloadTrip = e => {
    if (request.transportType === TransportTypeEnum.GROUP_TRANSFER && cancelStatuses.some(el => el === request.status)) {
      setVisibleReloadTripModal(true);
      return e.stopPropagation();
    }

    reloadTrip();
    e.stopPropagation();
  };

  const handleShowApprovedByModal = e => {
    setShowApprovedByModal(true);
    e.stopPropagation();
  };

  const reloadRouteAndAtriumTrip = () => {
    reloadTrip();
    tripStore.setGroupTransferInformation({
      information: { ...request.information, passengerCount: request.passengerCount },
      choosingTime: choosingTime,
      commentForPurpose: request.commentForPurpose,
    });
  };

  const onCancelRepeatRouteAndAtriumModal = () => {
    setVisibleRepeatRouteAndAtriumModal(false);
  };

  const onCancelShowApprovedByModal = () => {
    setShowApprovedByModal(false);
  };

  const onCancelReloadTripModal = () => {
    setVisibleReloadTripModal(false);
  };

  const busClass = busClasses.find(el => el === request.taxiClass);
  const cost = request.coopTrip && request.transportType === TransportTypeEnum.PERSONAL
    ? request.sharedRideOwner
    && request.expected.cost
      ? ((request.expected.cost) / 100)
      : (request.costSharePart && request.expected.cost) && ((request.expected.cost * request.costSharePart) / 100)
    : (request.transportType === TransportTypeEnum.TAXI && request.costSharePart && request.expected.cost)
      ? ((request.expected.cost * request.costSharePart) / 100)
      : request.expected?.cost && (request.expected?.cost / 100);

  const hasFraudComments = request.fraudComment && request.fraudComment.length !== 0;
  const hasFraud = request?.fraud?.comment || hasFraudComments;

  return (
    <List.Item onClick={(): void => onClickHandler(request?.id)} className={styles.listItem}>
      <div className={classNames(styles.contentWrapper, { [styles.fraudListItem]: hasFraud })}>
        { hasFraud && (
        <div className={styles.fraudContainer}>
          <WarningIcon />
          Нарушение правил оформления поездки
        </div>
        )}
        <div className={styles.wrapperListItem}>
          <div className={styles.header}>
            <div className={styles.drive}>
              <div className={styles.applicationNumber}>
                <span>{`Поездка ${request.humanReadableId}`}</span>
              </div>
              <div className={styles.purpose}>
                <span>{request?.purpose && request.purpose.label}</span>
              </div>
            </div>
            <div className={styles.dateInfo}>
              <div className={styles.date}>
                <span>{zoneTime(request?.desiredDate, DATE_FORMAT.DATE_WITH_TIME, request?.timeZone)}</span>
              </div>

              <StatusTripRequest
                withHint
                status={request?.status}
                type={request?.transportType}
              />
            </div>
          </div>
          <div className={styles.main}>
            <div className={styles.transportType}>
              <div className={styles.transportTypeWrapper}>
                <div className={styles.TransportPictureWrapper}>
                  <div className={classNames(busClass ? 'BUS' : request.transportType)} />
                </div>
                <span>{request.transportType && TransportTypeTitlesEnum[request.transportType]}</span>
              </div>
              <span className={styles.coopTrip}>
                {request.coopTrip ? 'Совместная поездка' : 'Индивидуальная поездка'}
              </span>
            </div>
            <div className={styles.costWrapper}>
              <span className={styles.costWrapper__title}>Стоимость услуг</span>
              <span className={styles.costWrapper__price}>{formatRublesWithoutRemainder(cost)}</span>
              {TAXES && (
                <span className={styles.costWrapper__text}>До вычета НДФЛ</span>
              )}
            </div>
          </div>
          <div className={styles.approvedBy} onClick={handleShowApprovedByModal}>
            <img src={Support} alt="Support" />
            Посмотреть согласующих
          </div>
          <div className={classNames(styles.borderLine, TAXES && styles['borderLine--withTaxes'])} />
          <div className={styles.footer}>
            <RouteMap addresses={request.expected.waypoints} />
            <div className={styles.footerButtons} onClick={e => e.stopPropagation}>
              <Button
                onClick={confirmReloadTrip}
                block
                className={styles.buttonReload}
              >
                <img src={Reload} alt="Reload" />
              </Button>
              {/* </Link> */}
              {(request.isFinished || request.status === TripStatusesEnum.PUBLIC_PAYMENT_DONE) && !request.isRated
              && (
              <Button
                onClick={e => { handleAssess(), e.stopPropagation(); }}
                block
                className={styles.button}
              >
                Оценить
              </Button>
              )}

              {/* доделать */}
              {/* {personalRequestIsApproved && isPersonalTransport && isNotSharedOwner && (
            <Button onClick={(e) => {startPersonalTrip(), e.stopPropagation()}} block className={styles.button}>
              Начать поездку
            </Button>
          )}
          {carsharingRequestIsApproved && isCarsharingTransport && (
            <Button onClick={(e) => {startCarsharingTrip(), e.stopPropagation()}} block className={styles.button}>
              Начать поездку
            </Button>
          )} */}
              {(personalTripInProgress || carsharingTripInProgress) && activeStatusEntity?.cancelable && (
              <Button
                disabled
                block
                onClick={e => e.stopPropagation}
                className={styles.button}
                htmlType="submit"
              >
                Завершить поездку
              </Button>
              )}
              {activeStatusEntity?.cancelable && request?.transportType === TransportTypeEnum.TAXI && <DeclineModal id={request.id} cancelHandler={cancelHandler} />}
              {activeStatusEntity?.cancelable && request?.transportType === TransportTypeEnum.GROUP_TRANSFER && (
                <DeclineTransferModalEmpty
                  cancelHandler={() => cancelHandler('Причина по умолчанию', request.id)}
                  cancelAndRepeatHandler={() => {
                    justCancelHandler('Причина по умолчанию', request.id);
                    reloadRouteAndAtriumTrip();
                  }}
                />
              )}
              {activeStatusEntity?.cancelable && (
                request?.transportType !== TransportTypeEnum.TAXI && request?.transportType !== TransportTypeEnum.GROUP_TRANSFER
              ) && (
                <DeclineModalEmpty cancelHandler={() => cancelHandler('Причина по умолчанию', request.id)} />
              )}
              {(request.isFinished || request.status === TripStatusesEnum.PUBLIC_PAYMENT_DONE) && !request.isRated && (
              <div onClick={e => e.stopPropagation()}>
                <RateModal
                  request={request}
                  isNotDetailedRequest={true}
                  setShowModal={setShowModal}
                  show={showModal}
                />
              </div>
              )}
              {request.requestRating?.rating
              && (
              <div className={styles.rate}>
                <Rate disabled defaultValue={request.requestRating?.rating} />
              </div>
              )}
              {visibleReloadTripModal && (
              <ReloadTripModal
                visible={visibleReloadTripModal}
                onCancel={onCancelReloadTripModal}
                reloadTrip={reloadTrip}
                setVisibleRepeatRouteAndAtriumModal={setVisibleRepeatRouteAndAtriumModal}
              />
              )}
              {visibleRepeatRouteAndAtriumModal && (
              <RepeatRouteAndAtriumModal
                visible={visibleRepeatRouteAndAtriumModal}
                onCancel={onCancelRepeatRouteAndAtriumModal}
                reloadTrip={reloadRouteAndAtriumTrip}
                request={request}
                setChoosingTime={setChoosingTime}
                choosingTime={choosingTime}
              />
              )}
              <div onClick={e => e.stopPropagation()}>
                <ApprovedByModal
                  onCancel={onCancelShowApprovedByModal}
                  visible={showApprovedByModal}
                  delegates={delegates}
                  supervisor={supervisor}
                  request={request}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </List.Item>
  );
};

export const RenderTripRequestListItem = React.memo(RenderTripRequestList);
