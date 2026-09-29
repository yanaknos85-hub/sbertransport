import { CheckOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { Button, Descriptions, List } from 'antd';
import moment from 'moment';
import React from 'react';

import { formatRubles, toRubles } from 'utils';

import { DATE_FORMAT } from 'constants/constants.app';

import { isInitRequest } from 'modules/EmployeeApp/components/LimitsPage/utils';

import {
  ILimitsStore,
  LIMIT_REQUEST_STATUS,
  LIMIT_TYPE,
  LimitRequestInfo,
  LimitRequestSavingObject,
  LimitSharing
} from 'stores/Limits/Limit.interface';
import { TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { UUID } from 'utils/io-ts';

import LimitByMonthForm from './EditLimitRequestForm/index';

import styles from './item.module.scss';

const getForm = (
  parentlimitId: UUID,
  sum: number,
  limitsStore: ILimitsStore,
  status: LIMIT_REQUEST_STATUS,
  viewDisabled: boolean
): JSX.Element => (
  <LimitByMonthForm
    key={parentlimitId}
    parentlimitId={parentlimitId}
    status={status}
    limitsStore={limitsStore}
    startSum={sum}
    viewDisabled={viewDisabled}
  />
);

export const getMoneyForm = (
  parentlimitId: UUID,
  sum: number,
  balance: number,
  limitsStore: ILimitsStore,
  status: LIMIT_REQUEST_STATUS,
  viewDisabled: boolean
): JSX.Element => {
  if (balance) {
    return sum <= balance
      ? getForm(parentlimitId, sum, limitsStore, status, viewDisabled)
      : getForm(parentlimitId, balance, limitsStore, status, viewDisabled);
  }
  return getForm(parentlimitId, sum, limitsStore, status, viewDisabled);
};

export const LimitRequestListItem: React.FC<{
  request: LimitRequestInfo;
  viewDisabled: boolean;
  limitsStore: ILimitsStore;
  itemClickHandler: any;
}> = ({
  request, viewDisabled, limitsStore, itemClickHandler,
}) => {
  const getDepBalance = (): LimitSharing | undefined => limitsStore.limitSharing.find(x => x.transportType === request.transportType);

  const checkLimitBalanceByTransport = (): boolean => {
    if (request.limitId) {
      const depLimitBalance = getDepBalance();
      return depLimitBalance ? request.sum < depLimitBalance.sum : false;
    }
    return false;
  };

  const { logger } = useAppStoreContext();

  // FIXME sonarjs/cognitive-complexity
  const getOperation = (): string => (request.limitId ? 'пополнение' : 'выделение');

  const getText = (limitType: LIMIT_TYPE): string => limitType === LIMIT_TYPE.DEPARTMENT
    ? `запрашивает согласование на ${getOperation()} лимита подразделения`
    : `запрашивает согласование на ${getOperation()} лимита сотрудника`;

  const {
    firstName, patronymic, lastName, humanReadableId,
  } = request.author;

  return (
    <List.Item
      className={styles.listItem}
      onClick={(): void => {
        itemClickHandler(request.id);
      }}
    >
      <div className={`${styles.info} ${styles.infoBottom}`}>
        <div>{moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}</div>
      </div>
      <div className={styles.infoBottom}>{request.humanReadableId}</div>
      <div className={styles.infoBottom}>
        <span className={styles.boldText}>
          {/* FIXME определить глобальный метод для получения строки в таком виде и использовать */}
          {`${firstName} ${patronymic} ${lastName} (${humanReadableId})`}
        </span>
      </div>
      {isInitRequest(request.status) && !viewDisabled && <div className={styles.infoBottom}>{getText(request.limitType)}</div>}

      {viewDisabled && <div className={styles.infoBottom}>{getText(request.limitType)}</div>}

      <Descriptions size="small" column={3}>
        <Descriptions.Item label="Вид транспорта" span={3}>
          <span>{TransportTypeTitlesEnum[request.transportType]}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Сумма запроса" span={3}>
          <span>{formatRubles(request.sum)}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Лимит на месяц (руб.)" span={3}>
          <span>{request.limitSum ? formatRubles(request.limitSum) : '-'}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Фактический остаток лимита на месяц (руб.)" span={3}>
          <span>{request.limitBalance ? formatRubles(request.limitBalance) : '-'}</span>
        </Descriptions.Item>

        <Descriptions.Item label="Лимит подразделения на месяц (руб.)" span={3}>
          <span>
            {getDepBalance() ? formatRubles(toRubles(getDepBalance()?.limitSharingPerPeriodDTO?.sum || 0, true)) : '-'}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Фактический остаток лимита подразделения на месяц (руб.)" span={3}>
          <span>
            {getDepBalance()
              ? formatRubles(toRubles(getDepBalance()?.limitSharingPerPeriodDTO?.balance || 0, true))
              : '-'}
          </span>
        </Descriptions.Item>
      </Descriptions>
      <div className={styles.approveBtnContainer}>
        <Button
          type="link"
          disabled={!checkLimitBalanceByTransport() || (request.limitId && (!isInitRequest || viewDisabled))}
          onClick={(e): Promise<number | void> => {
            e.stopPropagation();
            const data = {
              requestId: request.id,
              sum: request.sum,
              approvalState: 'APPROVED',
            } as LimitRequestSavingObject;
            return limitsStore.approveLimitRequest(data).catch(error => {
              logger.toMessage('error', error.response.data.message);
            });
          }}
          icon={<CheckOutlined />}
        />
        {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
        <div
          // FIXME jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
          className={styles.displayInline}
          onClick={(e): void => {
            e.stopPropagation();
          }}
        >
          {getMoneyForm(
            request.id,
            request.sum,
            getDepBalance()?.balance ?? 0,
            limitsStore,
            request.status,
            viewDisabled
          )}
        </div>
        <Button
          type="link"
          disabled={!isInitRequest || viewDisabled}
          onClick={(e): Promise<number> => {
            e.stopPropagation();
            const data = {
              requestId: request.id,
              sum: request.sum,
              approvalState: 'DECLINED',
            } as LimitRequestSavingObject;
            return limitsStore.approveLimitRequest(data);
          }}
          danger
          icon={<CloseCircleOutlined />}
        />
      </div>
    </List.Item>
  );
};
