import { Descriptions } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { FC, useEffect, useState } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';
import { MonthNames } from 'constants/calendar.constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TDepartment } from 'stores/Corporate/Corporate.interface';
import { LimitRequestStatusesTitlesEnum } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { formatRubles } from 'utils';

import { ISpentActionsType } from '../useDetailedLimitsMapper';
import { isInitRequest, isRequestCancelled } from '../utils';

import styles from './styles.module.scss';

const UserLimitRequestContent: FC<{ request: ISpentActionsType }> = observer(({ request }) => {
  // FIXME sonarjs/cognitive-complexity
  const { [StoreNames.corporateStore]: corporateStore } = useAppStoreContext();

  const [siblings, setSiblings] = useState<string[]>([]);
  const isSiblings: boolean = request.approverDtoList && request.approverDtoList.length > 1;

  useEffect(() => {
    if (isSiblings) {
      corporateStore.departments.forEach((dep: TDepartment): void => {
        request.approverDtoList.forEach((element: { departmentId: string }): void => {
          if (element.departmentId === dep.id) {
            setSiblings((prevArray: string[]) => [...prevArray, dep.departmentName]);
          }
        });
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [request]);
  // FIXME react-hooks/exhaustive-deps

  const {
    firstName, patronymic, lastName, humanReadableId,
  } = request.author;

  return (
    <>
      <div className={styles.infoBottom}>
        <div>{moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}</div>
      </div>
      <div className={styles.infoBottom}>{request.humanReadableId}</div>
      <div className={`${styles.infoBottom} ${styles.boldText}`}>
        {/* FIXME определить глобальный метод для получения строки в таком виде и использовать */}
        {`${firstName} ${patronymic} ${lastName} (${humanReadableId})`}
      </div>
      <Descriptions size="middle" column={3}>
        <Descriptions.Item label="Период" span={3}>
          <span>{MonthNames[request.period]}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Ресурс" span={3}>
          <span>
            {request.transportType
              ? TransportTypeTitlesEnum[request.transportType as keyof typeof TransportTypeTitlesEnum]
              : '-'}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Запрашиваемая сумма" span={3}>
          <span>{formatRubles(request.sum || 0)}</span>
        </Descriptions.Item>
        {isSiblings && (
          <Descriptions.Item label="Получатели" span={3}>
            <span>{siblings.join(', ')}</span>
          </Descriptions.Item>
        )}
        <Descriptions.Item label="Статус" span={3}>
          <span>
            {request.status
              ? LimitRequestStatusesTitlesEnum[request.status as keyof typeof LimitRequestStatusesTitlesEnum]
              : '-'}
          </span>
        </Descriptions.Item>
        {isRequestCancelled(request.status) && (
          <Descriptions.Item label="Причина" span={3}>
            <span>{request.declineReason || '-'}</span>
          </Descriptions.Item>
        )}
        {!isInitRequest(request.status) && (
          <Descriptions.Item label="Комментарий" span={3}>
            <span>{request.description || '-'}</span>
          </Descriptions.Item>
        )}
      </Descriptions>
    </>
  );
});

export default UserLimitRequestContent;
