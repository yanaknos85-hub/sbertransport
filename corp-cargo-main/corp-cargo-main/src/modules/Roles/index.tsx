import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';

import { useRoles } from 'api/roles';

import { EditableColumnType } from 'shared/components/EditableTable';

import { Role } from 'stores/Roles/Roles.interface';
import { Table } from 'antd';

import styles from './styles.module.scss';

type RoleRecord = Role & { id: string; isNew?: boolean; originalCode: string };

const useColumns = (): EditableColumnType<RoleRecord>[] => {
  const { t } = useTranslation();

  return useMemo(
    (): EditableColumnType<RoleRecord>[] => [
      {
        title: t.Roles.Columns.Code,
        dataIndex: 'code',
        key: 'code',
        width: 50,
      },
      {
        title: t.Roles.Columns.Name,
        dataIndex: 'name',
        key: 'name',
        width: 50,
      },
      {
        title: t.Roles.Columns.Description,
        dataIndex: 'description',
        key: 'description',
        width: 200,
      },
    ],
    [t]
  );
};

export const Roles = (): JSX.Element => {
  const { data } = useRoles();

  return (
    <Table
      columns={useColumns()}
      dataSource={data as RoleRecord[]}
      bordered
      size="middle"
      tableLayout="auto"
      rowKey="id"
      className={styles.table}
      pagination={false}
    />
  );
};

export default Roles;
