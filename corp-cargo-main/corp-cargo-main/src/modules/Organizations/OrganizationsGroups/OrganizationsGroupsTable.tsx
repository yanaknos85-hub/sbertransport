import React, { FC, useState } from 'react';

import { Button, Table } from 'antd';

import { OrganizationsGroup } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';
import { PlusOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';
import { useModalState } from 'shared/hooks/useModal';
import OrganizationsGroupModal from '../OrganizationsGroupModal';
import { useColumns } from './useColumns';

import styles from './OrganizationsGroupsTable.module.scss';

export const OrganizationsGroupsTable: FC<{
  organizationsGroups: OrganizationsGroup[];
  isFetching: boolean;
}> = ({ organizationsGroups, isFetching }) => {
  const [group, setGroup] = useState<OrganizationsGroup | undefined>();

  const columns = useColumns(setGroup);

  const { t } = useTranslation();

  const [isAddOpened, { show: showAdd, hide: hideAdd }] = useModalState();

  const onHideAdd = () => {
    hideAdd();
    setGroup(undefined);
  };

  return (
    <>
      <Table
        bordered
        loading={isFetching}
        columns={columns}
        dataSource={organizationsGroups}
        rowKey="id"
        size="small"
        tableLayout="auto"
        pagination={false}
        className={styles.groupsTable}
        footer={() => (
          <Button
            icon={<PlusOutlined />}
            size="middle"
            onClick={showAdd}
          >
            {t.global.addOrganizationsGroup}
          </Button>
        )}
      />
      <OrganizationsGroupModal
        group={group}
        visible={!!group || isAddOpened}
        onCancel={onHideAdd}
      />
    </>
  );
};
