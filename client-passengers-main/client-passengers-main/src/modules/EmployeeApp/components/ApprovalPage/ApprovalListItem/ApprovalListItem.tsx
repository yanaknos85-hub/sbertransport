import React, { type FC, type MouseEvent, useState } from 'react';
import { observer } from 'mobx-react';
import cn from 'classnames';
import { Button, Space, Divider } from 'antd';
import { useRouteMatch } from 'react-router-dom';

import { ApprovalModel } from 'shared/models/Approval.model';
import { DATE_FORMAT } from 'constants/constants.app';
import RouteMap from 'utils/RouteMap/RouteMap';
import { busClasses, IDeclineReason, TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';
import { TransportTypeEnum, TransportTypeHeaderTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { formatRublesWithoutRemainder } from 'utils/MoneyUtils';
import { zoneTimeISODate } from 'utils/trips/times';
import { formatFullName } from 'utils/formatFullName';
import { ApprovalTypeEnum } from 'shared/models/Approval.interface';
import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';
import ApprovalReasonModal from '../ApprovalReasonModal/ApprovalReasonModal';
import {
  useFinalTripApprove,
  useFinalTripDecline,
  useRequestApprove,
  useRequestDecline,
  useSharedRideRequestApprove
} from 'api/approvals';
import { ApprovalStatusBadge } from '../ApprovalStatusBadge/ApprovalStatusBadge';
import { useUpdatedTripApprove } from 'api/approvals-updated';

import styles from './approvalListItem.module.scss';
import { ApprovalFraudModal } from '../ApprovalFraudModal/ApprovalFraudModal';
import { useModalState } from 'shared/hooks/useModal';

interface ApprovalListItemProps {
  approval: ApprovalModel;
  view(approval: ApprovalModel): void;
  refetchApprovals(): void;
}

export const ApprovalListItem: FC<ApprovalListItemProps> = observer(({
  approval,
  view,
  refetchApprovals,
}): JSX.Element => {
  const {
    requestHumanReadableId, purposeLabel, desiredDate, timeZone,
  } = approval;

  const [reasonVisible, setReasonVisible] = useState(false);
  const [isFraudModalOpen, fraudModalActions] = useModalState();
  const toggleReasonModal = (): void => setReasonVisible(x => !x);
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { filter } = match.params;

  const [finalTripApprove] = useFinalTripApprove();
  const [requestApprove] = useRequestApprove();
  const [sharedRideApprove] = useSharedRideRequestApprove();
  const [updatedTripApprove] = useUpdatedTripApprove();
  const [finalTripDecline] = useFinalTripDecline();
  const [requestDecline] = useRequestDecline();

  const isTripApproval = approval.approvalType === ApprovalTypeEnum.TRIP;
  const isSharedRideApproval = approval.approvalType === ApprovalTypeEnum.SHARED_RIDE;
  const isUpdatedTripApproval = approval.approvalType === ApprovalTypeEnum.UPDATED_TRIP;

  const coopTripTitle = approval.coopTrip ? 'Совместная поездка' : 'Индивидуальная поездка';

  const approve = (event: MouseEvent<HTMLButtonElement>): void => {
    event.stopPropagation();

    const approveMethod
      = (isTripApproval && finalTripApprove)
      || (isSharedRideApproval && sharedRideApprove)
      || (isUpdatedTripApproval && updatedTripApprove)
      || requestApprove;
    approveMethod({ id: approval.id }).then(() => {
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

  const handleRedirectToApproval = () => {
    view(approval);
  };

  const handleDeclineRequest = (id: string, reason: IDeclineReason): void => {
    if (approval.approvalType === ApprovalTypeEnum.REQUEST) {
      requestDecline({ id, reason }).then(() => {
        toggleReasonModal();
        window.dispatchEvent(new Event('decline'));
      });
    } else if (approval.approvalType === ApprovalTypeEnum.TRIP) {
      finalTripDecline({ id, reason }).then(() => {
        toggleReasonModal();
        window.dispatchEvent(new Event('decline'));
      });
    }
  };

  const busClass = busClasses.find(el => el === approval.taxiClass);

  const isTaxi = !!approval.taxiClass && approval.transportType === TransportTypeEnum.TAXI;

  const formattedDesiredDate = zoneTimeISODate(desiredDate, DATE_FORMAT.DATE_WITH_TIME_DOTS, timeZone);

  const buttonIsDisabled = filter === 'closed';

  const fraudComments = approval?.fraudComment?.map(({ text }) => text) || [];
  const hasFraudComments = fraudComments.length !== 0;

  return (
    <div className={styles.approvalItemContainer}>
      <ApprovalReasonModal
        id={approval.id}
        visible={reasonVisible}
        onOk={handleDeclineRequest}
        onCancel={toggleReasonModal}
      />
      { hasFraudComments && (
      <ApprovalFraudModal
        open={isFraudModalOpen}
        onClose={fraudModalActions.hide}
        comment={fraudComments}
      />
      )}
      <div className={cn(styles.contentWrapper, { [styles.withFraudContentWrapper]: hasFraudComments })}>
        {hasFraudComments && (
        <div className={styles.fraudStatusContainer}>
          <WarningIcon />
          Нарушение правил оформления поездки
          <span className={styles.extraText} onClick={() => fraudModalActions.toggle()}>Подробнее</span>
        </div>
        )}
        <div className={cn(styles.approvalItem, { [styles.approvalItemWithFraud]: hasFraudComments })} onClick={handleRedirectToApproval}>
          <div className={styles.topSection}>
            <div className={styles.header}>
              <div className={styles.headerMain}>
                <span className={styles.headerTitle}>{ requestHumanReadableId }</span>
                <span className={styles.headerSubtitle}>{ purposeLabel }</span>
              </div>
              <div className={styles.headerTimeInfo}>
                <div className={styles.headerTimeStatus}>
                  <span>Желаемая дата и время поездки:</span>
                  <span>{formattedDesiredDate}</span>
                </div>
                <ApprovalStatusBadge status={approval.status} title={approval.getStatusString()} />
              </div>
            </div>
            <div className={styles.approvedBy}>
              {formatFullName(approval.passenger.firstName, approval.passenger.lastName, approval.passenger.patronymic)}
            </div>
            <div className={styles.mainContainer}>
              <div className={styles.transportInfoContainer}>
                <div className={styles.transportTypeWrapper}>
                  <div className={styles.transportIconWrapper}>
                    <div className={cn(busClass ? 'BUS' : approval.transportType)} />
                  </div>
                  <div className={styles.transportTypeBlock}>
                    <span className={styles.transportTypeBlockTitle}>{approval?.transportType && TransportTypeHeaderTitlesEnum[approval.transportType]}</span>
                    {isTaxi && <span className={styles.transportTypeBlockClassBadge}>{TaxiClassTitlesEnum[approval.taxiClass as keyof typeof TransportTypeEnum]}</span>}
                  </div>
                </div>
                <div className={styles.coopTripTitle}>
                  {coopTripTitle}
                </div>
              </div>
              <div className={styles.costWrapper}>
                <span className={styles.costWrapperTitle}>
                  Стоимость услуг
                </span>
                <span className={styles.costWrapperPrice}>
                  {formatRublesWithoutRemainder(approval.cost / 100)}
                </span>
              </div>
            </div>
          </div>
          <Divider />
          <div className={styles.footerContainer}>
            <RouteMap className={styles.routeMap} addresses={approval.waypoints} />
            <Space className={styles.buttonsContainer}>
              <Button
                onClick={onDecline}
                className={styles.actionButton}
                disabled={buttonIsDisabled}
              >
                Отклонить
              </Button>
              <Button
                onClick={approve}
                className={cn(styles.actionButton, styles.approveButton)}
                disabled={buttonIsDisabled}
              >
                Согласовать
              </Button>
            </Space>
          </div>
        </div>
      </div>
    </div>
  );
});
