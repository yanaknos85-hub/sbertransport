import React, { FC } from 'react';
import { Table } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { formatRubles } from 'utils';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { getEmpInitials, getSum } from '../util';

import styles from '../../styles.module.scss';

const columns = [
  {
    title: 'Подразделение',
    dataIndex: 'departmentId',
    key: 'departmentId',
  },
  {
    title: 'Выделенная сумма',
    dataIndex: 'sum',
    key: 'sum',
  },
  {
    title: 'ФИО согласующего',
    dataIndex: 'employeeId',
    key: 'employeeId',
  },
];

export const ApproversDataTable: FC<{
  request: LimitRequestInfo;
}> = observer(({ request }) => {
  const { [StoreNames.corporateStore]: corpStore, [StoreNames.employeeStore]: empStore } = useAppStoreContext();
  const { getDepartment } = corpStore;

  const dataSource: any[] = [];

  const updateTableData = (): void => {
    request.approverDtoList.forEach((x, index) => {
      dataSource.push({
        key: index,
        departmentId: getDepartment(x.departmentId)?.departmentName,
        sum: x.approvalState === 'DECLINED' ? 'Отклонено' : formatRubles(x.sum),
        employeeId: getEmpInitials(x.employeeId, empStore),
      });
    });
  };

  updateTableData();

  const footer = (amount: number): JSX.Element => (
    <>
      Итого:
      {formatRubles(amount)}
    </>
  );

  return (
    <Table
      className={styles.infoTop}
      dataSource={dataSource}
      columns={columns}
      footer={(): JSX.Element => footer(getSum(request))}
      size="small"
      pagination={{
        pageSize: 3,
      }}
    />
  );
});
