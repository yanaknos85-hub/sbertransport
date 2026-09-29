import { ColumnsType } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import React, { useMemo } from 'react';
import { useRouteMatch } from 'react-router-dom';
import AccessControl from 'shared/components/AccessControl';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { DepartmentDetailed } from 'stores/Department/Department.interface';
import { compareBy, stringSorter } from 'utils/sorting';
import { useRole } from 'utils/useRole';
import { Roles } from 'constants/constants.app';

export const useColumns = (): ColumnsType<DepartmentDetailed> => {
  const { t } = useTranslation();
  const match = useRouteMatch();
  const userRoles = useRole();

  return useMemo<ColumnsType<DepartmentDetailed>>(
    () => [
      {
        title: t.Departments.Id,
        dataIndex: 'humanReadableId',
        key: 'humanReadableId',
        fixed: 'left',
        sorter: compareBy('humanReadableId'),
      },
      {
        title: t.Departments.Name,
        dataIndex: 'departmentName',
        key: 'departmentName',
        sorter: compareBy('departmentName', stringSorter),
      },
      {
        title: t.Departments.Code,
        dataIndex: 'code',
        key: 'code',
        sorter: compareBy('code'),
      },
      {
        title: t.Departments.Head,
        dataIndex: 'departmentHead',
        key: 'departmentHead',
        sorter: compareBy('departmentHead'),
      },
      {
        title: t.Departments.Status,
        dataIndex: 'statusTitle',
        key: 'statusTitle',
        sorter: compareBy('status'),
      },
      {
        title: t.Departments.Organization,
        dataIndex: 'organization',
        key: 'organization',
        sorter: compareBy('organization'),
      },
      {
        title: t.Departments.ParentDepartment,
        dataIndex: 'parent',
        key: 'parent',
        sorter: compareBy('parent'),
      },
      {
        title: t.Departments.FullStructurePath,
        dataIndex: 'fullStructurePath',
        key: 'fullStructurePath',
        sorter: compareBy('fullStructurePath'),
      },
      {
        title: t.Departments.Location,
        dataIndex: 'location',
        key: 'location',
        sorter: compareBy('location'),
      },
      {
        title: t.Departments.EasupId,
        dataIndex: 'easupId',
        key: 'easupId',
        sorter: compareBy('easupId'),
      },
      {
        fixed: 'right',
        width: 60,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        render: (_: any, record: DepartmentDetailed): JSX.Element => (
          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={[Roles.ADMIN_DATA_MASTER, Roles.ADMIN_CORP_CLIENT]}
          >
            {record.humanReadableId ? (
              <TableEditButtons
                path={match.path}
                id={record.humanReadableId}
                title={t.global.deleteConfirm}
              />
            ) : (
              <></>
            )}
          </AccessControl>
        ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t]
  );
};
