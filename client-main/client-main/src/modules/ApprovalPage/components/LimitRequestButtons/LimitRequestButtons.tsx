import React, { useState, FC } from 'react';
import { useHistory } from 'react-router-dom';
import { observer } from 'mobx-react';
import { Button, Popconfirm } from 'antd';
import { CheckOutlined } from '@ant-design/icons';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as Delete } from 'shared/icons/delete.svg';
import { LimitRequestApprovalStateEnum } from 'stores/Limits/LimitsRequest.interface';
import { LIMIT_REQUEST_STATUS, LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { toRubles } from 'utils/MoneyUtils';
import LimitByMonthForm from '../EditLimitRequestForm';

interface LimitRequestButtonsProps {
  request: LimitRequestInfo;
  isDetailedPage?: boolean;
  viewDisabled?: boolean;
  refresh?: () => void;
}

const getRequestSum = (request: LimitRequestInfo): number => (
  toRubles(request.sum - request.approverDtoList.reduce((x, total) => total.sum + x, 0))
);

export const LimitRequestButtons: FC<LimitRequestButtonsProps> = observer(
  ({
    request, isDetailedPage, viewDisabled, refresh,
  }) => {
    const history = useHistory();
    const { [StoreNames.limitsStore]: limitsStore, logger } = useAppStoreContext();
    const [loading, setLoading] = useState('');

    const isInitRequest = request.status === LIMIT_REQUEST_STATUS.INIT;
    const depLimit = limitsStore.currentDepartmentSharing.find(x => x.transportType === request.transportType);
    const balance = depLimit?.balance ?? 0;
    const startSum = balance && !(request.sum <= balance) ? balance : request.sum;

    const goToList = (): void => {
      history.push('../');
    };

    const limitBalanceByTransport = depLimit ? request.sum < depLimit.sum : false;

    const handleApproveLimitRequest = (approvalState: LimitRequestApprovalStateEnum) => () => {
      setLoading(approvalState);

      limitsStore
        .approveLimitRequest({
          requestId: request?.id,
          sum: getRequestSum(request),
          approvalState,
        })
        .then(() => isDetailedPage ? goToList() : refresh?.())
        .catch(error => logger.toMessage('error', error.response.data.message))
        .finally(() => setLoading(''));
    };

    return (
      <>
        {isInitRequest && (
          <>
            <Button
              type="link"
              disabled={!limitBalanceByTransport || (!isInitRequest || viewDisabled)}
              onClick={handleApproveLimitRequest(LimitRequestApprovalStateEnum.APPROVED)}
              icon={<CheckOutlined color="#262626" />}
              loading={loading === LimitRequestApprovalStateEnum.APPROVED}
            />
            <LimitByMonthForm
              key={request.id}
              parentLimitId={request.id}
              status={request.status}
              limitsStore={limitsStore}
              startSum={startSum}
              viewDisabled={viewDisabled}
              refresh={refresh}
            />
            <Popconfirm
              placement="left"
              title="Отклонить заявку на лимит?"
              onConfirm={handleApproveLimitRequest(LimitRequestApprovalStateEnum.DECLINED)}
              okText="Да"
              cancelText="Отмена"
              style={{ width: 300 }}
            >
              <Button
                type="link"
                disabled={!isInitRequest || viewDisabled}
                icon={<Delete />}
                loading={loading === LimitRequestApprovalStateEnum.DECLINED}
              />
            </Popconfirm>
          </>
        )}
      </>
    );
  }
);
