import React, { FC, SyntheticEvent } from 'react';
import { Button, Col, Row } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { LimitRequestInfo, LimitRequestSavingObject } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { isInitRequest } from '../../../../LimitsPage/utils';
import { getMoneyForm } from '../../LimitRequestList/LimitRequestListItem';
import { useLimitRequestDetailedView } from '../useLimitRequestDetailedView';
import { getSum } from './util';

import styles from '../styles.module.scss';

export const LimitRequestButtons: FC<{ request: LimitRequestInfo }> = observer(({ request }) => {
  const { [StoreNames.limitsStore]: limitsStore, logger } = useAppStoreContext();
  const { goToList } = useLimitRequestDetailedView();
  const approveRequest = (e: SyntheticEvent): void => {
    e.stopPropagation();
    const data = {
      requestId: request?.id,
      sum: getSum(request),
      approvalState: 'APPROVED',
    } as LimitRequestSavingObject;
    limitsStore
      .approveLimitRequest(data)
      .then(goToList)
      .catch(error => {
        logger.toMessage('error', error.response.data.message);
      });
  };

  const getDepBalance = (): number | undefined => {
    const sharingByTransport = limitsStore.limitSharing.find(x => x.transportType === request.transportType);
    return sharingByTransport?.balance;
  };

  const checkLimitBalanceByTransport = (): boolean => {
    if (request.limitId) {
      const depLimitBalance = getDepBalance();
      return depLimitBalance ? request.sum < depLimitBalance : false;
    }
    return false;
  };

  return (
    <Row className={styles.mgTop}>
      <Col className={styles.alignLeft} span={8}>
        <Button disabled={!isInitRequest(request.status) || !checkLimitBalanceByTransport()} onClick={approveRequest}>
          Согласовать
        </Button>
      </Col>
      <Col className={styles.alignCenter} span={8}>
        {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
        <div
          // FIXME jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
          className={styles.moneyInfo}
          onClick={(e): void => {
            e.stopPropagation();
          }}
        >
          {getMoneyForm(request.id, request.sum, getDepBalance() ?? 0, limitsStore, request.status, false)}
        </div>
      </Col>
      <Col className={styles.alignRight} span={8}>
        <Button
          disabled={!isInitRequest(request.status)}
          onClick={(e): void => {
            e.stopPropagation();
            const data = {
              requestId: request.id,
              sum: request.sum,
              approvalState: 'DECLINED',
            } as LimitRequestSavingObject;
            limitsStore.approveLimitRequest(data);
            goToList();
          }}
          danger={true}
        >
          Отклонить
        </Button>
      </Col>
    </Row>
  );
});
