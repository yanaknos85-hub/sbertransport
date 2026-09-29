import React, { FC, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { PageHeader } from 'antd';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { MonthNames } from 'constants/calendar.constants';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { Stepper } from 'components/Limits';
import { formatName, upFirst } from 'utils/formatName';
import { formatRublesWithoutPennies, toRubles } from 'utils';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import {
  LIMIT_REQUEST_STATUS,
  LimitRequestInfo,
  LimitRequestStatusesTitlesEnum,
  LimitTypeTitles
} from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores';

import { LimitRequestButtons } from '../../components/LimitRequestButtons/LimitRequestButtons';
import styles from './styles.module.scss';

const LimitRequestDetailedView: FC<{ request: LimitRequestInfo }> = observer(({ request }) => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const { params } = useRouteMatch<{ filter: string }>();
  const { isDesktop } = usePlatformDetect();
  const history = useHistory();

  const isActiveFilter = params.filter === 'active';

  useEffect(() => {
    if (isActiveFilter && request && !limitsStore.currentDepartmentSharing.length) {
      limitsStore.initStore();
    }
  }, [limitsStore]);

  const goToList = (): void => {
    history.goBack();
  };

  const isRequestCancelled = request?.status === LIMIT_REQUEST_STATUS.CANCELLED;

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

  const creationTime
    = request?.creationTime && moment(request.creationTime).utcOffset('GMT+06').format(DATE_FORMAT.DATE_WITH_TIME);

  return request ? (
    <div className={styles.request}>
      <div className={styles.headWrapper}>
        <div className={styles.requestHeader}>
          <div>
            <PageHeader
              title={`${isDesktop ? 'Заявка на пополнение лимита ' : ''}${request.humanReadableId}`}
              onBack={goToList}
            />
            {creationTime && (
              <p className={styles.creationTime}>
                Создано
                {' '}
                <time>{creationTime}</time>
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
              <Stepper currentStatus={request.status as LIMIT_REQUEST_STATUS} subTitle={creationTime} />
            </div>
          )}
        </div>
        <div className={styles.actions}>
          <LimitRequestButtons request={request} isDetailedPage />
        </div>
      </div>
      <div className={styles.requestDetails}>
        <h4 className={styles.requestTitle}>Детали заявки</h4>
        <div className={styles.detailsList}>
          {detailsList.map(({ label, value }) => (
            <div key={value} className={styles.detailsCard}>
              <span className={styles.requestLabel}>{label}</span>
              <span className={styles.requestDescription}>{value}</span>
            </div>
          ))}
        </div>
      </div>
      <div className={styles.requestComment}>
        <h4 className={styles.requestTitle}>Комментарий</h4>
        <p className={styles.requestDescription}>{request.description || '-'}</p>
      </div>
      {isRequestCancelled && (
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

export default LimitRequestDetailedView;
