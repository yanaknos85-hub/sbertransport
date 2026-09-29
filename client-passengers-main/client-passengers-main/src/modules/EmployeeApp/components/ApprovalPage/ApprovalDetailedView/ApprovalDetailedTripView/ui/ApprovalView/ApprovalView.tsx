import React, {
  type FC,
  useEffect,
  useMemo,
  useState
} from 'react';
import { observer } from 'mobx-react';
import cn from 'classnames';
import {
  Button,
  PageHeader,
  Descriptions
} from 'antd';
import moment from 'moment';
import { useRouteMatch } from 'react-router-dom';

import { TripRequestModel } from 'stores/Trip/models';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { StoreNames } from 'stores';
import { busClasses } from 'stores/Trip/Trip.interface';
import { TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';

import { PageContent } from 'shared/components/PageContent/PageContent';
import { ReactComponent as CommentIcon } from 'shared/components/Images/comment.svg';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import User from 'shared/components/Images/user.png';
import { TicketsContainer } from './TicketsContainer/TicketsContainer';
import { TripFraudMarker } from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/TripFraudMarker/TripFraudMarker';
import { LimitsView } from './LimitsView/LimitsView';
import { useTripRequestRoute } from 'shared/hooks/trip';
import { TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';

import RouteMap from 'utils/RouteMap/RouteMap';
import RouteMapListCheckIn from 'utils/RouteMapListCheckIn/RouteMapListCheckIn';
import { formatFullName } from 'utils/formatFullName';
import { getDistanceString, getTimeString } from 'utils';

import { DATE_FORMAT } from 'constants/constants.app';

import styles from './approvalView.module.scss';

interface ApprovalViewProps {
  request: TripRequestModel;
  back: () => void;
  onCancel: () => void;
  onApprove: () => void;
}

export const ApprovalView: FC<ApprovalViewProps> = observer(({
  request,
  back,
  onApprove,
  onCancel,
}) => {
  const { [StoreNames.tripStore]: tripStore, [StoreNames.limitsStore]: limits } = useAppStoreContext();
  const [avatar, setAvatar] = useState('');
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { filter } = match.params;

  const { author } = request;

  useEffect(() => {
    limits.getLimits();

    tripStore.getUserAvatart(author.id)
      .then(setAvatar);
  }, []);

  const foundLimitsDTO = useMemo(() => {
    const filteredLimits = limits.currentDepartmentSharing.filter(limit => limit.transportType === request.transportType);

    return filteredLimits?.[0]?.limitSharingPerPeriodDTO;
  }, [limits.currentDepartmentSharing]);

  const isWithLimitsPage = typeof foundLimitsDTO?.sum === 'number' && typeof foundLimitsDTO?.balance === 'number';

  const waitingTime = getTimeString(request?.expected?.waypoints.reduce((acc, value) => acc + value.waitTime, 0) || 0);
  const expectedDistance = getDistanceString(request.expected.distance);
  const expectedTime = getTimeString(request.expected.time);
  const initiatorFio = formatFullName(author.firstName, author.lastName, author.patronymic);

  const transportTypeTitle = TransportTypeTitlesEnum[request.transportType as keyof typeof TransportTypeTitlesEnum];

  const formattedCreationTime = moment(request.creationTime).format(DATE_FORMAT.FULL_MONTH_NAME);
  const tripFormattedDate = moment(request.desiredDate).format(DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME);

  const busClass = busClasses.find(el => el === request.taxiClass);
  const coopTripStatusTitle = request.coopTrip ? 'cовместная поездка' : 'индивидуальная поездка';

  const { passengerCount } = request;

  const subTitle = [tripFormattedDate, coopTripStatusTitle, `пассажиров: ${passengerCount}`].join(' • ');

  const personalTripInProgress = request.status === TripStatusesEnum.PERSONAL_AWAITING_TRIP_APPROVAL;
  const tripRequestRoute = useTripRequestRoute(request);
  const checkingRouteManually = (personalTripInProgress)
    && request.transportType === TransportTypeEnum.PERSONAL
    && request.status;

  const buttonIsDisabled = filter === 'closed';
  const fraudComment = request?.fraud?.comment;
  const fraudCommments = request.fraudComment?.map(({ text }) => text) || [];
  const hasFraud = fraudComment || fraudCommments.length !== 0;

  return (
    <div className={styles.container}>
      <PageHeader
        className={styles.pageHeader}
        onBack={back}
        title={'Согласование заявки ' + request?.humanReadableId}
        subTitle={`от ${formattedCreationTime}`}
        extra={[
          <Button
            className={cn(styles.commonButton, styles.declineButton)}
            onClick={onCancel}
            disabled={buttonIsDisabled}
          >
            Отклонить
          </Button>,
          <Button
            className={cn(styles.commonButton, styles.submitButton)}
            type="primary"
            onClick={onApprove}
            disabled={buttonIsDisabled}
          >
            Согласовать
          </Button>,
        ]}
      >
        <div className={styles.headerContainer}>
          <div className={styles.leftSide}>
            <div className={styles.transportWrapper}>
              <div className={styles.transportIcon}>
                <div className={cn(busClass ? styles.BUS : request.transportType ? styles[request.transportType] : '')} />
              </div>
              <span className={styles.transportWrapperTitle}>{transportTypeTitle}</span>
            </div>
            <div className={styles.tariffDescription}>
              <div className={styles.accentDescriptionTitle}>
                {request.taxiClass && (
                <span className={styles.accentDescriptionTitleClass}>
                  тариф:
                  {' '}
                  {TaxiClassTitlesEnum[request.taxiClass].toLowerCase()}
                  {' • '}
                </span>
                )}
                <span>
                  {subTitle}
                </span>
              </div>
            </div>
          </div>
          <LimitsView
            cost={request.costInRubles as number}
            rest={foundLimitsDTO?.balance}
            sum={foundLimitsDTO?.sum}
            isWithLimitsPage={isWithLimitsPage}
          />
        </div>
      </PageHeader>
      <PageContent className={styles.pageWrapper}>
        {hasFraud && <TripFraudMarker comment={fraudComment || fraudCommments} isApprover />}
        <div className={styles.approvalWrapper}>
          <p className={styles.titleText}>Маршрут</p>
          {checkingRouteManually ? (
            <RouteMapListCheckIn request={request} tripRequestRoute={tripRequestRoute} />
          )
            : (
              <RouteMap addresses={request.expected.waypoints} />
            )}
        </div>
        <div className={styles.tripDescription}>
          <div className={styles.approvalWrapper}>
            <p className={styles.title}>Инициатор</p>
            <div className={styles.personDataContainer}>
              <div className={styles.userAvatarWrapper}>
                <img alt="avatar" src={request.transportType === 'TAXI' ? User : avatar ? avatar : User} />
              </div>
              <div className={styles.personalInfoBlock}>
                <span className={styles.accentText}>{initiatorFio}</span>
                {author.personnelNumber && (
                  <span className={styles.subTitle}>
                    Таб №
                    {author.personnelNumber}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className={styles.approvalWrapper}>
            <p className={styles.title}>
              Цель поездки
            </p>
            <span>{request.purpose.label}</span>
            {request?.commentForPurpose && (
            <div className={styles.commentForPurpose}>
              <CommentIcon className={styles.purposeIcon} />
              <span>
                {request.commentForPurpose}
              </span>
            </div>
            )}
          </div>
        </div>

        {request.transportCompensation && (
          <TicketsContainer request={request} />
        )}

        <div className={styles.approvalWrapper}>
          <p className={styles.title}>Плановые данные</p>
          <Descriptions
            labelStyle={{ color: 'rgb(168, 171, 179)' }}
            layout="vertical"
            colon={false}
          >
            <Descriptions.Item label="Время в пути">
              {expectedTime}
            </Descriptions.Item>
            <Descriptions.Item label="Время ожидания">
              {waitingTime}
            </Descriptions.Item>
            <Descriptions.Item label="Расстояние">
              {expectedDistance}
            </Descriptions.Item>
          </Descriptions>
        </div>
      </PageContent>
    </div>
  );
});
