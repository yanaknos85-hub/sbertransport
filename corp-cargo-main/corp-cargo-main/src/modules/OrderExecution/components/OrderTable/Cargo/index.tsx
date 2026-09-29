import React, { FC, useRef, useState } from 'react';
import { observer } from 'mobx-react';
import { toJS } from 'mobx';
import { Table } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { SorterResult } from 'antd/lib/table/interface';
import { FeedCargoContentType } from 'stores/Engineer/Models/FeedCargo/Feed.content';
import { useTableConfig } from 'shared/hooks/useTableConfig';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { Pagination } from 'shared/components/PaginationWithPageSelect';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { OrderCancelModal } from './OrderCancelModal';
import { useTableFields } from './useTableFields';
import { tableScrollConfiguration } from '../../../utils/common';

import styles from './styles.module.scss';

const CargoOrdersTable: FC = () => {
  const { cargoStore } = useAppStoreContext();

  const [reason, setReason] = useState('');
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
    form.setFieldsValue({ reason: '' });
  };

  const handleTableChange = (
    _: unknown,
    __: unknown,
    sorter: SorterResult<FeedCargoContentType> | SorterResult<FeedCargoContentType>[]
  ) => {
    if (Array.isArray(sorter)) return;

    if (sorter.columnKey === 'humanReadableId') {
      if (sorter.order) {
        const order = sorter.order === 'ascend' ? 'asc' : 'desc';
        cargoStore.setCargoSortOrder('humanReadableId', order);
      } else {
        cargoStore.setCargoSortOrder(null, null);
      }
      cargoStore.getCargoOrderListDeferredPost();
    }
  };

  return (
    <div ref={tableRef}>
      {cargoStore.cargoOrderList ? (
        <>
          <Table
            rowKey="id"
            columns={fields}
            dataSource={toJS(cargoStore.cargoOrderList.content)}
            className={styles.ordersTable}
            rowClassName={styles.ordersTable__row}
            scroll={tableConfig}
            pagination={false}
            onChange={handleTableChange}
          />
          <Pagination
            pagination={{ page: cargoStore.cargoOrderList.number, size: cargoStore.cargoOrderList.size }}
            total={cargoStore.cargoOrderList.totalElements}
            setPagination={cargoStore.setCargoPageSetting}
          />
        </>
      ) : <SpinWrapped />}
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
    </div>
  );
};

export default observer(CargoOrdersTable);
