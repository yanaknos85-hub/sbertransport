import React, { FC, useRef, useState } from 'react';
import { observer } from 'mobx-react';
import { toJS } from 'mobx';
import { Table } from 'antd';
import { useForm } from 'antd/lib/form/Form';

import { useTableConfig } from 'shared/hooks/useTableConfig';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Pagination } from 'shared/components/PaginationWithPageSelect';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { OrderCancelModal } from '../../OrderTable/Cargo/OrderCancelModal';
import { useTableFields } from './SchedulerTableContent/useTableFields';
import { tableScrollConfiguration } from '../../../utils/common';

import styles from './styles.module.scss';

const SchedulerTable: FC = () => {
  const { cargoStore } = useAppStoreContext();

  const [reason, setReason]  = useState('');
  const [orderId, setOrderId] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [textAreaVisible, setTextAreaVisible] = useState(false);

  const tableRef = useRef<HTMLDivElement>(null);

  const [form] = useForm();

  const fields = useTableFields({ setOrderId, setModalVisible });
  const tableConfig = useTableConfig(tableRef, tableScrollConfiguration);

  const handleCancel = () => {
    setReason('');
    setTextAreaVisible(false);
    setModalVisible(false);
    form.setFieldsValue({reason: ''});
  };

  return (
    <React.Suspense fallback={<SpinWrapped/>}>
      <div ref={tableRef}>
        {cargoStore.cargoSchedulerList ? (
          <>
            <Table
              rowKey="id"
              columns={fields}
              dataSource={toJS(cargoStore.cargoSchedulerList.content)}
              className={styles.ordersTable}
              rowClassName={styles.ordersTable__row}
              scroll={tableConfig}
              pagination={false}
            />
            <Pagination
              pagination={{page: cargoStore.cargoSchedulerList.number, size: cargoStore.cargoSchedulerList.size}}
              total={cargoStore.cargoSchedulerList.totalElements}
              setPagination={cargoStore.setCargoSchedulerPageSetting}
            />
          </>
        ) : (
          <SpinWrapped/>
        )}
      </div>
      <OrderCancelModal
        form={form}
        orderId={orderId}
        reason={reason}
        setReason={setReason}
        onCancel={handleCancel}
        visible={modalVisible}
        textAreaVisible={textAreaVisible}
        setTextAreaVisible={setTextAreaVisible}
        setModalVisible={setModalVisible}
      />
    </React.Suspense>
  );
};

export default observer(SchedulerTable);
