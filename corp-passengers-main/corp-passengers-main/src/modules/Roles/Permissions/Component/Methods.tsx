import React, {
  FC, useEffect, useState, useRef, Suspense
} from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Table } from 'antd';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';

import { useMethodRoles } from 'api/roles';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import { Method } from 'stores/Roles/Roles.interface';
import styles from './styles.module.scss';

interface Cell<T> {
  render?: (_: string, record: T) => string;
  key: string;
  dataIndex: string;
}
const columns: Cell<Method>[] = [
  {
    dataIndex: 'description',
    key: 'description',
    render: (_, record) => `${record.name} - ${record.description || 'Описание не добавлено'}`,
  },
];

const EmptyMethodsTable = () => (
  <div className={styles.fixedHeader}>
    <div className={styles.tableHeader} />
    <SpinWrapped />
  </div>
);
// TODO: extract to shared? refactor? how to simplify react-query suspense usages across project?
class CustomErrorBoundary extends ErrorBoundary {
  clearErrors() {
    this.setState({ error: null });
  }
}

const MethodsTable: FC<{ service: string }> = React.memo(({ service }) => {
  const match = useRouteMatch<{ role: string }>();
  const { role } = match.params;

  const { data: serviceMethods, isLoading: isLoadingMethodsByService } = useMethodRoles(service, null);
  const { data: roleMethods, isLoading: isLoadingMethodsByRole } = useMethodRoles(service, role);

  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  useEffect(() => {
    setSelectedRowKeys(roleMethods.map(r => r.id));
  }, [roleMethods]);

  return serviceMethods ? (
    <div className={styles.fixedHeader}>
      <div className={styles.tableHeader}>
        <span className={styles.title}>
          Методы сервиса "
          {service}
          "
        </span>
      </div>
      <Table
        rowSelection={{ selectedRowKeys, hideSelectAll: true }}
        columns={columns}
        dataSource={serviceMethods}
        sticky
        pagination={false}
        rowKey="id"
        showHeader={false}
      />
      {(isLoadingMethodsByService || isLoadingMethodsByRole) && <SpinWrapped mask />}
    </div>
  ) : null;
});

export const Methods: FC<{ service?: string }> = ({ service }) => {
  const errorRef = useRef<CustomErrorBoundary>(null);

  useEffect(() => {
    if (errorRef) {
      errorRef.current?.clearErrors();
    }
  }, [service, errorRef]);

  if (!service) {
    return <div />;
  }

  return (
    <CustomErrorBoundary ref={errorRef}>
      <Suspense fallback={<EmptyMethodsTable />}>
        <MethodsTable service={service} />
      </Suspense>
    </CustomErrorBoundary>
  );
};
