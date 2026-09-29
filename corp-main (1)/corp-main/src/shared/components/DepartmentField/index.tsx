import React, { useState, useEffect } from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';
import { Form, TreeSelect } from 'antd';

import { useTranslation } from 'i18n';
import { buildDepartmentTree, ITreeNode } from 'utils/buildTreeSelectData';
import { useGetDepartmentList } from 'api/organizations/search';
import ChevronSmall from 'shared/images/menu 2.0/ChevronSmall';
import { UUID } from 'utils/io-ts';

import styles from './styles.module.scss';

interface IDepartmentFieldProps {
  filterOrganizationId?: string[];
  disabledfilterOrganizationId: boolean;
}

const DepartmentField: FC<IDepartmentFieldProps> = ({ filterOrganizationId, disabledfilterOrganizationId }) => {
  const { t: { global: { selectDepartment } } } = useTranslation();
  const [treeData, setTreeData] = useState<ITreeNode[]>([]);
  const organizationId = filterOrganizationId && filterOrganizationId[0];
  const [getDepartments] = useGetDepartmentList(organizationId);

  useEffect(() => {
    if (filterOrganizationId) {
      getDepartments({ orgId: filterOrganizationId[0] as UUID }).then(result => {
        if (result && result.length) {
          setTreeData(buildDepartmentTree(result));
        }
      });
    }
  }, [filterOrganizationId]);

  return (
    <Form.Item name="departmentIds" style={{ width: '75%', margin: '0 12px 0 0' }}>
      <TreeSelect
        treeCheckable
        suffixIcon={(
          <div style={{ borderLeft: '1px solod grey' }}>
            <ChevronSmall />
          </div>
        )}
        showArrow
        maxTagCount="responsive"
        className={styles.departmentField}
        placeholder={selectDepartment}
        disabled={disabledfilterOrganizationId}
        treeData={treeData}
        showCheckedStrategy={TreeSelect.SHOW_ALL}
        filterTreeNode={(value, treeNode) => value.length > 2 && typeof treeNode?.title === 'string'
          ? treeNode.title.toLowerCase().includes(value.toLowerCase())
          : true}
      />
    </Form.Item>
  );
};

export default observer(DepartmentField);
