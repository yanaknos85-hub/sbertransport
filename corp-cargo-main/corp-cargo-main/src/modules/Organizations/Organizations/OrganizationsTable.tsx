import React, { FC } from 'react';

import { Table } from 'antd';

import { Organization } from 'stores/Organizations/Organizations.interface';
import * as tt from 'utils/io-ts';
import { useColumns } from './useColumns';
import Footer from './Footer';

interface Row {
  id: tt.UUID;
  officialName: string;
  address: string;
  contacts?: any[];
  msrn?: string;
  digitId?: number;
  organizationCode?: number;
  organizationGroup?: string;
  tid?: string;
  easupId?: string;
}

const org2row = (org: Organization): Row => ({
  id: org.id,
  officialName: org.officialName,
  address: org.address,
  contacts: org.contacts,
  organizationCode: org.organizationCode,
  organizationGroup: org.organizationGroup && org.organizationGroup.name,
  msrn: org.msrn,
  tid: org.tid,
  easupId: org.easupId,
});

export const OrganizationsTable: FC<{
  organizations: Organization[];
  isFetching: boolean;
}> = ({ organizations, isFetching }) => {
  const columns = useColumns();

  return (
    <Table
      bordered
      loading={isFetching}
      columns={columns}
      dataSource={organizations.map(org2row)}
      rowKey="id"
      scroll={{ x: 3000 }}
      size="small"
      pagination={false}
      tableLayout="auto"
      footer={() => <Footer />}
    />
  );
};
