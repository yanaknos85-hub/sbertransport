import React, { FC, useEffect } from 'react';
import { Table } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatRubles } from 'utils';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';

import { maxLimitRows } from '../../LimitRequestContent';
import { getEmpInitials, getLimitHumanReadableId } from '../util';

import styles from '../../styles.module.scss';

const columns = [
  {
    title: 'Дата операции',
    dataIndex: 'creationTime',
    key: 'creationTime',
  },
  {
    title: 'Автор',
    dataIndex: 'author',
    key: 'author',
  },
  {
    title: 'Сумма',
    dataIndex: 'sum',
    key: 'sum',
  },
  {
    title: 'Исходный лимит',
    dataIndex: 'sourceLimit',
    key: 'sourceLimit',
  },
  {
    title: 'Целевой лимит',
    dataIndex: 'targetLimit',
    key: 'targetLimit',
  },
  {
    title: 'Исходный вид транспорта',
    dataIndex: 'sourceTransportType',
    key: 'sourceTransportType',
  },
  {
    title: 'Целевой вид транспорта',
    dataIndex: 'sourceTransportType',
    key: 'sourceTransportType',
  },
];

export const LimitTransfersTable: FC<{
  request: LimitRequestInfo;
}> = observer(({ request }) => {
  const { [StoreNames.limitsStore]: limitsStore, [StoreNames.employeeStore]: empStore } = useAppStoreContext();

  const dataSource: any[] = [];

  useEffect(() => {
    if (request.limitId) {
      limitsStore.getDepartmentLimits();
      limitsStore.getEmployeeLimits();
      limitsStore.getLimitTransferHistory(request.limitId, request.year, maxLimitRows);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limitsStore]);
  // FIXME react-hooks/exhaustive-deps

  const data = limitsStore.limitTransferHistory;

  if (data) {
    data.forEach((x, index) => {
      dataSource.push({
        key: index,
        author: getEmpInitials(x.author, empStore),
        creationTime: moment(x.creationTime).format(DATE_FORMAT.DATE_WITH_TIME),
        sum: formatRubles(x.sum),
        sourceLimit: getLimitHumanReadableId(x.sourceLimit, limitsStore) ?? x.sourceLimit,
        targetLimit: getLimitHumanReadableId(x.targetLimit, limitsStore) ?? x.targetLimit,
        sourceTransportType: TransportTypeTitlesEnum[x.sourceTransportType],
        targetTransportType: TransportTypeTitlesEnum[x.targetTransportType],
        year: x.year,
      });
    });
  }

  return (
    <Table
      className={styles.infoTop}
      dataSource={dataSource}
      columns={columns}
      size="small"
      pagination={{
        pageSize: 3,
      }}
    />
  );
});
