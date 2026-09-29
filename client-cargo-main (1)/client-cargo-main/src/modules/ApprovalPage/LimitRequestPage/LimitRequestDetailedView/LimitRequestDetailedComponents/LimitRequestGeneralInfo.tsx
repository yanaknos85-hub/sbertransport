import React, { FC } from 'react';
import { Descriptions } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { LimitBar } from 'shared/components/LimitBar/LimitBar';
import { emptySign } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatRubles, toRubles } from 'utils';

import { LIMIT_TYPE, LimitRequestInfo, LimitSharing } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import { formatPercents } from 'utils/Misc';

import { LimitRequestPeriods } from '../../../../LimitsPage/LimitRequestModal/constants';

import styles from '../styles.module.scss';

export const LimitRequestGeneralInfo: FC<{ request: LimitRequestInfo }> = observer(({ request }) => {
  const percent
    = request && request?.limitBalance && request?.limitSum
      ? Number(((request?.limitBalance * 100) / request?.limitSum).toFixed(1))
      : 0;
  const {
    firstName, patronymic, lastName, humanReadableId,
  } = request.author;
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const getDepBalance = (): LimitSharing | undefined => limitsStore.limitSharing.find(x => x.transportType === request.transportType);

  return (
    <>
      <div className={styles.infoBottom}>
        <div>{moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}</div>
      </div>
      <div className={styles.infoBottom}>{request.humanReadableId}</div>
      <div className={styles.infoBold}>
        {/* FIXME определить глобальный метод для получения строки в таком виде и использовать */}
        {`${firstName} ${patronymic} ${lastName} (${humanReadableId})`}
      </div>
      <Descriptions size="small" column={3}>
        <Descriptions.Item label="Табельный номер" span={3}>
          <span>{request.author.personnelNumber}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Номер лимита" span={3}>
          <span>{request.limitHumanreadableid}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Период" span={3}>
          <span>{LimitRequestPeriods[request.period]}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Вид транспорта" span={3}>
          <span>{TransportTypeTitlesEnum[request.transportType]}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Запрашиваемая сумма" span={3}>
          <span>{formatRubles(request.sum)}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Плановое значение" span={3}>
          <span>{request.plannedSum ? formatRubles(request.plannedSum) : ''}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Лимит на месяц (руб.)" span={3}>
          <span>{request.limitSum ? formatRubles(request.limitSum) : emptySign}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Фактический остаток лимита на месяц (руб.)" span={3}>
          <span>{request.limitBalance ? formatRubles(request.limitBalance) : emptySign}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Лимит подразделения на месяц (руб.)" span={3}>
          <span>
            {getDepBalance() ? formatRubles(toRubles(getDepBalance()?.limitSharingPerPeriodDTO?.sum || 0, true)) : emptySign}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Фактический остаток лимита подразделения на месяц (руб.)" span={3}>
          <span>
            {getDepBalance()
              ? formatRubles(toRubles(getDepBalance()?.limitSharingPerPeriodDTO?.balance || 0, true))
              : emptySign}
          </span>
        </Descriptions.Item>
      </Descriptions>

      <div className={`${styles.infoTop} ${styles.infoBottom}`}>
        <LimitBar
          limitType={LIMIT_TYPE.DEPARTMENT}
          percent={percent}
          showInfo={false}
          className={styles.limit_bar}
        />
        <div className={styles.percentInfo}>{formatPercents(percent / 100)}</div>
      </div>
    </>
  );
});
