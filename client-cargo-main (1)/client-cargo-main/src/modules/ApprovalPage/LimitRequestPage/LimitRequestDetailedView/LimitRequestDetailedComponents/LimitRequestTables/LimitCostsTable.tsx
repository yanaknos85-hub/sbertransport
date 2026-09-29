import React, { FC, useEffect } from 'react';
import { Table } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatRubles } from 'utils';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { maxLimitRows } from '../../LimitRequestContent';
import { getLimitHumanReadableId } from '../util';

import styles from '../../styles.module.scss';

const columns = [
  {
    title: 'Лимит',
    dataIndex: 'limitId',
    key: 'limitId',
  },
  {
    title: 'Выделенная сумма',
    dataIndex: 'sumReserved',
    key: 'sumReserved',
  },
];

export const LimitCostsTable: FC<{
  request: LimitRequestInfo;
}> = observer(({ request }) => {
  const { [StoreNames.limitsStore]: limitsStore, [StoreNames.selfStore]: selfStore } = useAppStoreContext();

  const dataSource: any[] = [];

  useEffect(() => {
    if (request.limitId) {
      limitsStore.getDepartmentLimits();
      limitsStore.getEmployeeLimits();
      limitsStore.getLimitCostHistory(request.limitId, maxLimitRows, selfStore.orgId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limitsStore]);
  // FIXME react-hooks/exhaustive-deps

  const updateTableData = (): void => {
    const data = limitsStore.limitCostHistory;
    if (data) {
      data.forEach((x, index) => {
        dataSource.push({
          key: index,
          limitId: getLimitHumanReadableId(x.limitId, limitsStore) ?? x.limitId,
          sumReserved: formatRubles(x.sumReserved),
        });
      });
    }
  };

  updateTableData();

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
