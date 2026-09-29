import { EditOutlined, FileDoneOutlined, FileExcelOutlined } from '@ant-design/icons';
import {
  Button, Descriptions, List, Tooltip
} from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { useState } from 'react';
import NumberFormat from 'react-number-format';
import { useRouteMatch } from 'react-router-dom';

import {
  useFinalTripApprove, useRequestApprove, useRequestDecline, useSharedRideRequestApprove
} from 'api/approvals';
import { useUpdatedTripApprove } from 'api/approvals-updated';

import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';

import { RouteIcon } from 'shared/components/SvgIcons';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ApprovalTypeEnum } from 'shared/models/Approval.interface';
import { ApprovalModel } from 'shared/models/Approval.model';
import { IDeclineReason } from 'stores/Trip/Trip.interface';

import ApprovalReasonModal from '../ApprovalReasonModal/ApprovalReasonModal';

import listStyles from '../list.module.scss';
import styles from './styles.module.scss';

const ApprovalItem = observer(
  ({
    approval,
    view,
    refetchApprovals,
  }: {
    approval: ApprovalModel;
    view(approval: ApprovalModel): void;
    refetchApprovals(): void;
  }): JSX.Element => {
    const { employeeStore } = useAppStoreContext();

    const match = useRouteMatch<{ filter: 'active' | 'closed' }>();
    const { filter } = match.params;

    const cost = `${approval.getCostInRubles(true)} ${RUBLE_SIGN}`;
    const balance = approval.restOfLimit;

    const [finalTripApprove] = useFinalTripApprove();
    const [requestApprove] = useRequestApprove();
    const [requestDecline] = useRequestDecline();
    const [sharedRideApprove] = useSharedRideRequestApprove();
    const [updatedTripApprove] = useUpdatedTripApprove();

    const isTripApproval = approval.approvalType === ApprovalTypeEnum.TRIP;
    const isSharedRideApproval = approval.approvalType === ApprovalTypeEnum.SHARED_RIDE;
    const isUpdatedTripApproval = approval.approvalType === ApprovalTypeEnum.UPDATED_TRIP;

    const approve = (): void => {
      const approveMethod
        = (isTripApproval && finalTripApprove)
        || (isSharedRideApproval && sharedRideApprove)
        || (isUpdatedTripApproval && updatedTripApprove)
        || requestApprove;
      approveMethod({ id: approval.id }).then(() => {
        refetchApprovals();
      });
    };

    const [reasonVisible, setReasonVisible] = useState(false);
    const toggleReasonModal = (): void => setReasonVisible(x => !x);

    const transportTypeCasted = `${approval.transportType}` as keyof typeof styles;

    const getEmployeeShortName = (): string => {
      if (approval?.passengerId) {
        return employeeStore.employeeListByOrgMapped[approval.passengerId]?.shortNameWithNumberString;
      }
      return '';
    };

    return (
      <List.Item className={listStyles.listItem}>
        <ApprovalReasonModal
          id={approval.id}
          visible={reasonVisible}
          onOk={(id, reason: IDeclineReason): void => {
            requestDecline({ id, reason }).then(() => {
              toggleReasonModal();
            });
          }}
          onCancel={toggleReasonModal}
        />
        <div className={listStyles.authorName}>
          {getEmployeeShortName()}
          {isTripApproval && (
            <Tooltip title="Утверждение маршрута">
              <RouteIcon />
            </Tooltip>
          )}
          {isUpdatedTripApproval && (
            <Tooltip title="Изменённая заявка на согласование">
              <EditOutlined />
            </Tooltip>
          )}
        </div>
        <div className={listStyles.info}>
          <div>{moment(approval.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME)}</div>
          <div className={listStyles.cost}>{cost}</div>
        </div>
        <Descriptions
          size="small"
          className={listStyles.detailsList}
          column={3}
        >
          <Descriptions.Item label="Вид транспорта" span={3}>
            <span>{approval.transportTypeString}</span>
            <div
              className={classNames(
                (approval.taxiClass && styles[approval.taxiClass as keyof typeof styles])
                || (transportTypeCasted && styles[transportTypeCasted]),
                styles.taxiClass
              )}
            />
          </Descriptions.Item>
          <Descriptions.Item label="Адрес начала поездки" span={3}>
            <span>{`${approval.waypoints[0].addressStringWithRegion ? approval.waypoints[0].addressStringWithRegion : ''}`}</span>
          </Descriptions.Item>
          <Descriptions.Item label="Статус" span={3}>
            {approval.getStatusString()}
          </Descriptions.Item>
          <Descriptions.Item label="Цель" span={3}>
            {approval.purposeLabel ?? 'не выбрана'}
          </Descriptions.Item>
          <Descriptions.Item label="Остаток лимита" span={3}>
            <NumberFormat
              value={Math.round((balance || 0) / 100)}
              displayType="text"
              thousandSeparator="&thinsp;"
              suffix={` ${RUBLE_SIGN}`}
            />
          </Descriptions.Item>
        </Descriptions>
        <div className={listStyles.buttonBlock}>
          <>
            <Button
              onClick={(): void => view(approval)}
              block={true}
              icon={<FileDoneOutlined />}
            >
              Просмотреть
            </Button>

            {filter === 'active' && (
              <>
                <Button
                  type="primary"
                  onClick={approve}
                  icon={<FileDoneOutlined />}
                >
                  Согласовать
                </Button>
                <Button
                  danger={true}
                  onClick={(): void => toggleReasonModal()}
                  icon={<FileExcelOutlined />}
                >
                  Отклонить
                </Button>
              </>
            )}
          </>
        </div>
      </List.Item>
    );
  }
);

export default ApprovalItem;
