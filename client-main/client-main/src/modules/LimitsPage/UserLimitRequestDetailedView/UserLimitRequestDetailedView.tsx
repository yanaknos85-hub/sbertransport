/* eslint-disable jsx-a11y/label-has-for */
import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { PageHeader } from 'antd';
import moment from 'moment';

import { MonthNames } from 'constants/calendar.constants';
import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Stepper } from 'components/Limits';
import { StoreNames } from 'stores/StoreNames.enum';
import { LIMIT_REQUEST_STATUS, LimitRequestStatusesTitlesEnum, LimitTypeTitles } from 'stores/Limits/Limit.interface';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import { isInitRequest, isRequestCancelled } from '../utils';
import { formatRublesWithoutPennies, toRubles } from 'utils/MoneyUtils';
import { formatName, upFirst } from 'utils/formatName';
import UserLimitRequestApproveModal from './UserLimitRequestModal/UserLimitRequestApproveModal';
import useMyLimitsRequestsDetailedView from './useUserLimitRequestDetailedView';
import styles from './styles.module.scss';

const UserLimitRequestDetailedView: FC = observer(() => {
  const { [StoreNames.limitsStore]: limits } = useAppStoreContext();
  const { isDesktop } = usePlatformDetect();
  const { limitsRequestsByAuthor, limitsRequestsIsLoading } = limits;

  const match = useRouteMatch<any>();
  const { reqId } = match.params;

  const request = limitsRequestsByAuthor.find(x => x.id === reqId);

  const { goToList } = useMyLimitsRequestsDetailedView();

  if (limitsRequestsIsLoading) {
    return <SpinWrapped />;
  }

  const detailsList = [
    {
      label: 'Период',
      value: upFirst(MonthNames[request?.period]),
    },
    {
      label: 'Вид транспорта',
      value: TransportTypeTitles[request?.transportType] ?? '-',
    },
    {
      label: 'Тип лимита',
      value: LimitTypeTitles[request?.limitType] ?? '-',
    },
    {
      label: 'Запрашиваемая сумма',
      value: request?.sum ? formatRublesWithoutPennies(toRubles(request.sum)) : 0,
    },
    {
      label: 'Заявитель',
      value: formatName(request?.author),
    },
  ];

  return request ? (
    <div className={styles.request}>
      <div className={styles.headWrapper}>
        <div className={styles.requestHeader}>
          <div>
            <PageHeader
              title={`${isDesktop ? 'Заявка на пополнение лимита ' : ''}${request.humanReadableId}`}
              onBack={goToList}
            />
            {request.creationTime && (
              <p className={styles.creationTime}>
                Создано
                {' '}
                <time>{moment(request.creationTime).format(DATE_FORMAT.BASE_REVERTED_DOTS)}</time>
              </p>
            )}
          </div>
        </div>
        <div className={styles.statusContainer}>
          <p className={styles.statusName}>
            Статус заявки:
            <span>
              {' '}
              {LimitRequestStatusesTitlesEnum[request.status]}
            </span>
          </p>
          {request.status !== LIMIT_REQUEST_STATUS.INIT && (
            <div className={styles.statusStepper}>
              <Stepper
                currentStatus={request.status as LIMIT_REQUEST_STATUS}
                subTitle={
                  request.creationTime && moment(request.creationTime).format(DATE_FORMAT.BASE_REVERTED_DATE_DOTS)
                }
              />
            </div>
          )}
        </div>
        {isInitRequest(request.status) && (
          <div className={styles.requestActions}>
            <UserLimitRequestApproveModal
              isEdit={true}
              type={request.limitType}
              transportType={request.transportType}
              request={request}
              goToList={goToList}
            />
            <UserLimitRequestApproveModal
              isEdit={false}
              type={request.limitType}
              transportType={request.transportType}
              request={request}
              goToList={goToList}
            />
          </div>
        )}
      </div>
      <div className={styles.requestDetails}>
        <h4 className={styles.requestTitle}>Детали заявки</h4>
        <div className={styles.detailsList}>
          {detailsList.map(({ label, value }) => (
            <div key={value} className={styles.detailsCard}>
              <label className={styles.requestLabel}>{label}</label>
              <span className={styles.requestDescription}>{value}</span>
            </div>
          ))}
        </div>
      </div>
      <div className={styles.requestComment}>
        <h4 className={styles.requestTitle}>Комментарий</h4>
        <p className={styles.requestDescription}>{request.description || '-'}</p>
      </div>
      {isRequestCancelled(request.status) && (
        <div className={styles.requestComment}>
          <h4 className={styles.requestTitle}>Причина отказа</h4>
          <p className={styles.requestDescription}>{request.declineReason || '-'}</p>
        </div>
      )}
    </div>
  ) : (
    <EmptyDataList title="Заявка с таким номером отсутствует в системе" />
  );
});

export default UserLimitRequestDetailedView;
