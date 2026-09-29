import React, { FC, Suspense, useEffect } from 'react';
import { observer } from 'mobx-react';
import { toJS } from 'mobx';
import { Table } from 'antd';

import { useOrganizationContext } from 'context/Organization.context';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Pagination } from 'shared/components/PaginationWithPageSelect';

import type { OrderExecutionColumnProps } from './types';

import styles from '../../../styles.module.scss';

interface IProps {
  columns: OrderExecutionColumnProps[];
}

const PassengerTable: FC<IProps> = ({ columns }) => {
  const { passengerStore } = useAppStoreContext();
  const {
    organizationId, isOrganization, executorGroupId,
  } = useOrganizationContext();

  useEffect(() => {
    // @ts-ignore
    passengerStore.setOrganizationId(organizationId);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId]);

  useEffect(() => {
    // @ts-ignore
    passengerStore.setExecutorGroupId(executorGroupId);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [executorGroupId]);

  useEffect(() => {
    // @ts-ignore
    passengerStore.setIsOrganization(isOrganization);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOrganization]);

  useEffect(() => {
    if (!passengerStore.personQueryFilters?.transportType) return;

    passengerStore.setPersonFilterQueryProps({
      ...passengerStore.personQueryFilters,
      transportType: passengerStore.personQueryFilters.transportType,
    });

    if (isOrganization) {
      passengerStore.getPersonOrderList();
    } else {
      passengerStore.getPersonOrderListExec();
    }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId, executorGroupId, isOrganization]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={
          isOrganization || executorGroupId.length
            ? toJS(passengerStore.personOrderList?.content ?? [])
            : []
        }
        loading={passengerStore.isLoadingOrderList}
        className={styles.repairOrdersTable}
        rowClassName={styles.repairOrdersTable__row}
        scroll={{ x: '100%' }}
        pagination={false}
      />
      <Pagination
        pagination={{
          page: passengerStore.personOrderList?.number ?? 1,
          size: passengerStore.personOrderList?.size ?? 10,
        }}
        total={passengerStore.personOrderList?.totalElements ?? 1}
        setPagination={passengerStore.setPersonPageSetting}
        disabled={passengerStore.isLoadingOrderList}
      />
    </Suspense>
  );
};

export default observer(PassengerTable);
