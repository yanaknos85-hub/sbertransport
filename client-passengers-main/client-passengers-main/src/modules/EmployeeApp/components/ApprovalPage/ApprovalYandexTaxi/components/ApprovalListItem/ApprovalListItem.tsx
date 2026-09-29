import React, { type FC, type MouseEvent, useState } from 'react';
import { observer } from 'mobx-react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import moment from 'moment';
import classNames from 'classnames';
import { Button, Space, Divider } from 'antd';

import { DATE_FORMAT } from 'constants/constants.app';
import RouteMap from 'utils/RouteMap/RouteMap';
import { formatFullName } from 'utils/formatFullName';
import { formatRubles } from 'utils';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { YandexTaxiRequest } from 'api/yandexTaxi/yandex-taxi.types';
import { YandexTaxiRequestStatus, YandexTaxiRequestStatusTitles, YandexTaxiTariffTitles } from 'api/yandexTaxi/yandex-taxi.constants';
import { useYandexTaxiUpdateStatus } from 'api/yandexTaxi/yandex-taxi.api';

import ApprovalReasonModal from '../ApprovalReasonModal/ApprovalReasonModal';
import { ApprovalStatusBadge } from '../ApprovalStatusBadge/ApprovalStatusBadge';
import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';
import { COST_DEVIATION_TRESHOLD } from '../../constants/yandexTaxi.constants';
import { isFactCostOverTreshold } from '../../utils/data';
import { CostDeviationWarning } from '../CostDeviationWarning/CostDeviationWarning';
import { Icon } from '../Icon/Icon';
import { TripPurpose } from '../TripPurpose/TripPurpose';

import styles from './approvalListItem.module.scss';
import { ApprovalFraudModal } from '../../../ApprovalFraudModal/ApprovalFraudModal';
import { useModalState } from 'shared/hooks/useModal';
import { getFraudComments } from 'utils/fraud/getFraudComments';

interface ApprovalListItemProps {
  approval: YandexTaxiRequest;
  refetchApprovals(): void;
}

export const ApprovalListItem: FC<ApprovalListItemProps> = observer(({
  approval,
  refetchApprovals,
}): JSX.Element => {
  const {
    humanReadableId, passenger, purposeId, tripDate, plannedCost, factCost,
  } = approval;
  const status = approval.status as YandexTaxiRequestStatus;
  const isCostDeviationOverTreshold = isFactCostOverTreshold(plannedCost, factCost, COST_DEVIATION_TRESHOLD);
  const [reasonVisible, setReasonVisible] = useState(false);
  const [isFraudModalOpen, fraudModalActions] = useModalState();
  const toggleReasonModal = (): void => setReasonVisible(x => !x);
  const match = useRouteMatch<{ filter: 'active' | 'closed' }>();
  const { filter } = match.params;
  const history = useHistory();
  const [updateStatus] = useYandexTaxiUpdateStatus();
  const waypoints = approval?.waypoints?.map(point => new WaypointModel(point)) || [];

  const approveHandler = (event: MouseEvent<HTMLButtonElement>): void => {
    event.stopPropagation();

    updateStatus({
      id: approval.id as string, query: {
        factCost: approval.factCost || 0, status: YandexTaxiRequestStatus.CONFIRMED, reason: '',
      },
    }).then(() => {
      refetchApprovals();
      window.dispatchEvent(new Event('approve'));
    }).catch(() => {
      refetchApprovals();
    });
  };

  const onDecline = (event: MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();

    toggleReasonModal();
  };

  const navigateToDetailedView = () => {
    const targetPath = `${match.url}/${approval.id}`;
    history.push(targetPath);
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
        toggleReasonModal();
        window.dispatchEvent(new Event('decline'));
      });
  };

  const formattedDesiredDate = moment(tripDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS);
  const areButtonsVisible = filter === 'active';
  const fraudComments = getFraudComments(approval?.fraudComment);
  const hasFraud = fraudComments.length !== 0;

  return (
    <div className={classNames(styles.approvalItemContainer, { [styles.fraudWrapper]: hasFraud })}>
      <ApprovalReasonModal
        id={approval.id as string}
        visible={reasonVisible}
        onOk={handleDeclineRequest}
        onCancel={toggleReasonModal}
      />
      {hasFraud && (
        <ApprovalFraudModal
          open={isFraudModalOpen}
          onClose={fraudModalActions.hide}
          comment={fraudComments}
        />
      )}
      { hasFraud && (
        <div className={styles.fraudContainer}>
          <WarningIcon />
          Нарушение правил оформления поездки
          <span
            onClick={() => fraudModalActions.show()}
            className={styles.extraText}
          >
            Подробнее
          </span>
        </div>
      )}
      <div className={styles.approvalItem} onClick={navigateToDetailedView}>
        <div className={styles.topSection}>
          <div className={styles.header}>
            <div className={styles.headerMain}>
              <span className={styles.headerTitle}>{ humanReadableId }</span>
              {purposeId && <TripPurpose className={styles.headerSubtitle} purposeId={purposeId} />}
            </div>
            <div className={styles.headerTimeInfo}>
              <div className={styles.headerTimeStatus}>
                <span>Дата и время поездки:</span>
                <span>{formattedDesiredDate}</span>
              </div>
              <ApprovalStatusBadge status={status} title={YandexTaxiRequestStatusTitles[status]} />
            </div>
          </div>
          <div className={styles.mainContainer}>
            <div className={styles.transportInfoContainer}>
              <div className={styles.passengerWrapper}>
                <div className={styles.passengerName}>
                  {formatFullName(passenger?.firstName, passenger?.lastName, passenger?.patronymic)}
                </div>
              </div>
              <div className={styles.transportTypeWrapper}>
                <div className={styles.transportIcon}>
                  <Icon type="yandexGo" />
                </div>
                <div className={styles.transportTypeBlock}>
                  <span className={styles.transportTypeBlockTitle}>Яндекс Go</span>
                  <span className={styles.transportTypeBlockClassBadge}>{YandexTaxiTariffTitles[approval.tariff]}</span>
                </div>
              </div>
            </div>
            <div className={styles.costWrapper}>
              <span className={styles.costWrapperTitle}>
                Стоимость услуг
              </span>
              <div className={styles.withWarning}>
                {isCostDeviationOverTreshold && <CostDeviationWarning />}

                <span className={styles.costWrapperPrice}>
                  {formatRubles(approval.factCost)}
                </span>
              </div>
            </div>
          </div>
        </div>
        <Divider />
        <div className={styles.footerContainer}>
          <RouteMap className={styles.routeMap} addresses={waypoints} />
          {areButtonsVisible && (
            <Space className={styles.buttonsContainer}>
              <Button
                onClick={onDecline}
                className={styles.actionButton}
              >
                Отклонить
              </Button>
              <Button
                onClick={approveHandler}
                className={classNames(styles.actionButton, styles.approveButton)}
              >
                Согласовать
              </Button>
            </Space>
          )}
        </div>
      </div>
    </div>
  );
});
