import React, { FC } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { observer } from 'mobx-react';
import { PlusCircleOutlined } from '@ant-design/icons';
import { Button, List } from 'antd';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypesDelegates/TransportTypes.interface';
import { useProfile } from 'api/profile';
import { useDeleteDelegates, useGetDelegates } from 'api/delegates';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from './constants/EmployeeApp.constants';
import { DelegatesTexts, DelegatesTextsCyrillic } from './constants/Delegates.constants';
import useUrl from 'shared/hooks/useUrl';

import style from './delegates.module.scss';
import './override.scss';

const DelegatesList: FC = observer(() => {
  const { data } = useProfile();
  const history = useHistory();
  const { superviserId, departmentId } = useUrl();
  const [deleteDelegate] = useDeleteDelegates();

  const { data: delegatesData } = useGetDelegates(
    {
      orgId: data.organizationId as string,
      depId: departmentId as string,
      supId: superviserId as string,
    },
    {
      enabled: data.organizationId && departmentId && superviserId,
    }
  );

  const goToNewDelegate = () => {
    history.push(`/directories/departments/newDelegate?superviserId=${superviserId}&departmentId=${departmentId}`);
  };

  const handleEdit = (delegate: Delegate) => {
    history.push(
      `/directories/departments/delegates/${delegate.id}?superviserId=${superviserId}&departmentId=${departmentId}`
    );
  };

  return (
    <PageLayout title={EmployeeAppLinksTitles[EmployeeAppLinks.delegates]}>
      <List
        className="delegate_page"
        dataSource={delegatesData}
        renderItem={(delegate: Delegate): JSX.Element => {
          const person = `${delegate.delegateEmployee.lastName} ${delegate.delegateEmployee.firstName} ${delegate.delegateEmployee.patronymic}`;
          return (
            <List.Item
              actions={[
                <TableEditButtons
                  onEdit={() => handleEdit(delegate)}
                  path="delegates"
                  id={delegate.id}
                  onDelete={() => {
                    deleteDelegate({
                      orgId: data.organizationId as string,
                      depId: departmentId as string,
                      delId: delegate.id,
                    });
                  }}
                  title={DelegatesTextsCyrillic[DelegatesTexts.deleteConfirm]}
                  okText={DelegatesTextsCyrillic[DelegatesTexts.remove]}
                  cancelText={DelegatesTextsCyrillic[DelegatesTexts.cancel]}
                />,
              ]}
            >
              <List.Item.Meta
                key={delegate.id}
                title={person}
                description={TransportTypeTitlesEnum[delegate.transportType as TransportTypeEnum]}
              />
            </List.Item>
          );
        }}
      />
      <Button
        icon={<PlusCircleOutlined />}
        type="primary"
        size="middle"
        onClick={goToNewDelegate}
        className={style.assign_button}
      >
        {DelegatesTextsCyrillic[DelegatesTexts.addNew]}
      </Button>
    </PageLayout>
  );
});

export default DelegatesList;
