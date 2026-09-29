import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import classNames from 'classnames';
import { Button, PageHeader } from 'antd';
import moment from 'moment';
import { observer } from 'mobx-react';

import { StoreNames, useAppStore } from 'stores';
import * as routes from 'constants/constants.routes';
import { DATE_FORMAT } from 'constants/constants.app';
import { SpinWrapped } from 'shared/components';
import { UUID } from 'utils/io-ts';
import { formatFullName } from 'utils/formatFullName';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { PageContent } from 'shared/components/PageContent/PageContent';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import RouteMap from 'utils/RouteMap/RouteMap';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import ArrowRight from 'shared/components/Images/arrowRight.svg';
import { ReactComponent as CommentIcon } from 'shared/components/Images/comment.svg';

import { YandexTaxiRequestStatus, YandexTaxiTariffTitles } from 'api/yandexTaxi/yandex-taxi.constants';
import { useYandexTaxiRequestById, useYandexTaxiUpdateStatus } from 'api/yandexTaxi/yandex-taxi.api';
import { useAllEmployeesById, useEmployeeAvatar } from 'api/employees/employees.api';

import ApprovalReasonModal from '../ApprovalReasonModal/ApprovalReasonModal';
import { COST_DEVIATION_TRESHOLD } from '../../constants/yandexTaxi.constants';
import { isFactCostOverTreshold } from '../../utils/data';
import { CostDeviationWarning } from '../CostDeviationWarning/CostDeviationWarning';
import { Receipt } from './Receipt/Receipt';
import { Icon } from '../Icon/Icon';
import { TripPurpose } from '../TripPurpose/TripPurpose';
import { ApprovalYandexLimitView } from '../ApprovalYandexLimitView/ApprovalYandexLimitView';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripFraudMarker } from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/TripFraudMarker/TripFraudMarker';

import styles from './approvalYandexDetailedView.module.scss';
import { getFraudComments } from 'utils/fraud/getFraudComments';

const ApprovalYandexDetailedView: FC = observer(() => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStore();
  const match = useRouteMatch<{ id: string; filter: 'active' | 'closed' }>();
  const { id, filter } = match.params;
  const history = useHistory();
  const { data: approval, isLoading } = useYandexTaxiRequestById(id);
  const {
    humanReadableId, passengerId, tripDate, factCost, plannedCost, purposeId,
  } = approval;
  const passenger = useAllEmployeesById([passengerId as UUID]).data.employeeResponse.content[0];
  const { data: avatarSrc } = useEmployeeAvatar(passengerId as UUID);
  const [isDeclneModalVisible, setIsDeclneModalVisible] = useState(false);
  const [updateStatus] = useYandexTaxiUpdateStatus();
  const waypoints = approval?.waypoints?.map(point => new WaypointModel(point)) || [];
  const formattedTripDate = moment(tripDate).format(DATE_FORMAT.FULL_MONTH_DATE_WITH_TIME);

  const isCostDeviationOverTreshold = isFactCostOverTreshold(plannedCost, factCost, COST_DEVIATION_TRESHOLD);
  const areButtonsVisible = filter === 'active';

  const goBack = () => {
    history.push(`${routes.APPROVEMENT_YANDEX}/${filter || ''}`);
  };

  const toggleDeclineModalVisible = (): void => setIsDeclneModalVisible(x => !x);

  const foundLimitsDTO = useMemo(() => {
    const filteredLimits = limitsStore.currentDepartmentSharing.filter(limit => limit.transportType === TransportTypeEnum.TAXI);

    return filteredLimits?.[0]?.limitSharingPerPeriodDTO;
  }, [limitsStore.currentDepartmentSharing]);

  const isWithLimitsPage = typeof foundLimitsDTO?.sum === 'number' && typeof foundLimitsDTO?.balance === 'number';

  const approveHandler = (event: React.MouseEvent): void => {
    event.stopPropagation();

    updateStatus({
      id: approval.id as string,
      query: {
        factCost: approval.factCost || 0,
        status: YandexTaxiRequestStatus.CONFIRMED,
        reason: '',
      },
    }).then(() => {
      goBack();
    });
  };

  useEffect(() => {
    limitsStore.getLimits();
  }, []);

  const onDecline = (event: React.MouseEvent) => {
    event.stopPropagation();
    toggleDeclineModalVisible();
  };

  const handleDeclineRequest = (id: string, reason: IDeclineReason): void => {
    updateStatus({
      id,
      query: {
        factCost: approval.factCost || 0,
        status: YandexTaxiRequestStatus.DECLINED,
        reason: reason.reason,
      },
    })
      .then(() => {
        toggleDeclineModalVisible();
        goBack();
      });
  };
  const tabTitle = filter === 'active' ? 'Активные' : 'Завершённые';
  const handleFilterTabs = () => {
    history.push(`${routes.APPROVEMENT_YANDEX}/${filter || ''}`);
  };

  const fraudComments = getFraudComments(approval?.fraudComment);
  const hasFraud = fraudComments.length !== 0;

  return (
    isLoading ? (
      <SpinWrapped />
    ) : (
      <div>
        <div className={styles.headerBreadcrumbs}>
          <span className={styles.foolBreadcrumb}>Согласования </span>
          <img src={ArrowRight} alt="ArrowRight" />
          <span onClick={() => history.push(`${routes.APPROVEMENT_YANDEX}/active`)}>Яндекс Go</span>
          <img src={ArrowRight} alt="ArrowRight" />
          <span onClick={handleFilterTabs}>{tabTitle}</span>
          <img src={ArrowRight} alt="ArrowRight" />
          <span className={styles.foolBreadcrumb}>{`Поездка ${humanReadableId}`}</span>
        </div>

        <PageHeader
          className={styles.pageHeader}
          onBack={goBack}
          title={'Согласование заявки ' + humanReadableId}
          subTitle={`от ${moment(tripDate).format(DATE_FORMAT.MONTH_NAME)}`}
          extra={areButtonsVisible && [
            <Button
              className={classNames(styles.commonButton, styles.declineButton)}
              onClick={onDecline}
            >
              Отклонить
            </Button>,
            <Button
              className={classNames(styles.commonButton, styles.submitButton)}
              type="primary"
              onClick={approveHandler}
            >
              Согласовать
            </Button>,
          ]}
        >
          <div className={styles.headerContainer}>
            <div className={styles.leftSide}>

              <div className={styles.transportWrapper}>
                <Icon type="yandexGo" className={styles.transportIcon} />
                <span className={styles.transportWrapperTitle}>Яндекс Go</span>
              </div>

              <div className={styles.tariffDescription}>
                <div>
                  <span className={styles.accentDescriptionTitleClass}>
                    тариф:
                    {' '}
                    {YandexTaxiTariffTitles[approval.tariff]}
                    {' • '}
                    <span className={styles.accentDescriptionTitle}>
                      {formattedTripDate}
                    </span>
                  </span>
                </div>
              </div>
            </div>

            <div className={styles.rightSide}>
              {isWithLimitsPage && (
              <ApprovalYandexLimitView
                cost={approval.factCost as number}
                rest={foundLimitsDTO.balance}
                sum={foundLimitsDTO.sum}
                plannedCost={plannedCost as number}
              />
              )}
              {isCostDeviationOverTreshold && <CostDeviationWarning />}
            </div>
          </div>
        </PageHeader>

        <PageContent className={styles.pageWrapper}>
          { hasFraud && <TripFraudMarker comment={fraudComments} isApprover />}
          <div className={styles.approvalWrapper}>
            <p className={styles.titleText}>Маршрут</p>
            <RouteMap addresses={waypoints} />
          </div>
          <div className={styles.tripDescription}>
            <div className={styles.approvalWrapper}>
              <p className={styles.title}>Инициатор</p>
              <div className={styles.personDataContainer}>
                <div className={styles.passengerAvatar}>
                  {avatarSrc ? <img src={avatarSrc} alt="avatar" /> : '-__-'}
                </div>
                <div className={styles.personalInfoBlock}>
                  <span className={styles.accentText}>
                    {formatFullName(passenger?.firstName, passenger?.lastName, passenger?.patronymic)}
                  </span>
                  <span className={styles.subTitle}>
                    {`Таб.№ ${passenger.personnelNumber ?? '-'}`}
                  </span>
                </div>
              </div>
            </div>

            <div className={styles.approvalWrapper}>
              <p className={styles.title}>
                Цель поездки
              </p>
              {purposeId && <TripPurpose className={styles.headerSubtitle} purposeId={purposeId} />}
              {approval?.comment && (
              <div className={styles.commentForPurpose}>
                <CommentIcon className={styles.purposeIcon} />
                <span>
                  {approval.comment}
                </span>
              </div>
              )}
            </div>
          </div>

          {approval.receipt && (
            <div className={styles.approvalWrapper}>
              <p className={styles.title}>Прикрепленные файлы</p>
              <Receipt requestId={approval.id as UUID} fileName={approval.receipt} />
            </div>
          )}
        </PageContent>

        <ApprovalReasonModal
          id={approval.id as string}
          visible={isDeclneModalVisible}
          onOk={handleDeclineRequest}
          onCancel={toggleDeclineModalVisible}
        />

      </div>
    )
  );
});

export default withErrorBoundary(ApprovalYandexDetailedView);
