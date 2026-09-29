/* eslint-disable no-use-before-define */
import React, { FC, Suspense } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useTranslation } from 'i18n';
import { Button, Table } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import { PlusOutlined } from '@ant-design/icons';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useCargoAuto, useDeleteCargoAuto } from 'api/cargo-auto';
import { useColumns, CargoAutoRecord } from './Components/Columns';

import styles from './CargoAuto.module.scss';

const CargoAutoHandbookComponent: FC = () => (
  <Suspense fallback={<SpinWrapped />}>
    <TheTable />
  </Suspense>
);

// eslint-disable-next-line @typescript-eslint/ban-types
const TheTable: FC<{}> = () => {
  const { t } = useTranslation();

  const { cargoAuto } = useCargoAuto().data;
  const [deletePosition] = useDeleteCargoAuto();
  const match = useRouteMatch();

  const editButtons: ColumnProps<CargoAutoRecord> = {
    fixed: 'right',
    width: 60,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    render: (_: any, record: CargoAutoRecord): JSX.Element => (
      <TableEditButtons
        path={match.path}
        id={record.id}
        onDelete={() => deletePosition({ autoId: record.id })}
        title={t.global.deleteConfirm}
      />
    ),
  };

  const columnsProps = columnsPropsFactory<CargoAutoRecord>([...useColumns(), editButtons]);

  return (
    <Table
      columns={columnsProps}
      dataSource={cargoAuto}
      bordered
      scroll={{ x: 600 }}
      rowKey="id"
      size="small"
      tableLayout="auto"
      footer={() => <Footer />}
      className={styles.table}
    />
  );
};

const Footer = React.memo(() => {
  const { t } = useTranslation();
  const history = useHistory();
  const match = useRouteMatch();

  const handleAddNewClick = (): void => {
    history.push(`${match.path}/adding`);
  };

  return (
    <div className="table_footer">
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={handleAddNewClick}
      >
        {t.global.add}
      </Button>
    </div>
  );
});

export default CargoAutoHandbookComponent;
