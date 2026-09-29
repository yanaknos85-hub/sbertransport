import React, { FC } from 'react';
import { Table } from 'antd';
import { Method, Service } from 'stores/Roles/Roles.interface';
import { useServiceRoles } from 'api/roles';
import classNames from 'classnames';

import styles from './styles.module.scss';

interface Props {
  onServiceSelected: React.Dispatch<Service['id']>;
}

const useServiceColumns = (onServiceSelected: React.Dispatch<string>) => [
  {
    dataIndex: 'name',
    key: 'name',
    render: (text: string, record: Method) => (
      <div onClick={() => onServiceSelected(text)}>{`${text} - ${record.description || 'Описание не добавлено'}`}</div>
    ),
  },
];

export const Services: FC<Props> = ({ onServiceSelected }) => {
  const { data: service } = useServiceRoles(null);

  return (
    <div className={classNames(styles.fixedHeader, styles.services)}>
      <div className={styles.tableHeader}>
        <span className={styles.title}>Сервисы</span>
      </div>
      <Table
        columns={useServiceColumns(onServiceSelected)}
        dataSource={service}
        pagination={false}
        rowKey="id"
        sticky
        showHeader={false}
        className={styles.table}
      />
    </div>
  );
};
