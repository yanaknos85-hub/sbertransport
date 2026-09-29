import React, { FC } from 'react';
import { useParams } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { observer } from 'mobx-react';

import { List } from 'antd';
import PageLayout from 'shared/components/PageLayout/PageLayout';

import useUrl from 'shared/hooks/useUrl';
import { DelegatesTexts, DelegatesTextsCyrillic } from './constants/Delegates.constants';
import { useGetDelegate } from 'api/delegates';
import { useProfile } from 'api/profile';
import { TransportTypeTitlesEnum } from 'stores/TransportTypesDelegates/TransportTypes.interface';

import './override.scss';

const DelegateCard: FC = observer(() => {
  const history = useHistory();
  const { data } = useProfile();
  const { id } = useParams();
  const { superviserId, departmentId } = useUrl();

  const { data: delegate } = useGetDelegate({
    orgId: data.organizationId,
    depId: departmentId as string,
    delId: id,
  });

  return (
    <PageLayout
      title={DelegatesTextsCyrillic[DelegatesTexts.pageHeader]}
      onBack={() => {
        history.push(`/directories/departments/delegates?superviserId=${superviserId}&departmentId=${departmentId}`);
      }}
    >
      <List>
        <List.Item>
          <List.Item.Meta
            title={`${delegate.delegateEmployee.lastName} ${delegate.delegateEmployee.firstName} ${delegate.delegateEmployee.patronymic}`}
          />
        </List.Item>
        <List.Item>
          <List.Item.Meta title={`C ${delegate.startDate} по ${delegate.endDate}`} description="Период делегирования" />
        </List.Item>
        {delegate.transportType && (
          <List.Item>
            <List.Item.Meta title={TransportTypeTitlesEnum[delegate.transportType]} description="Вид транспорта" />
          </List.Item>
        )}
      </List>
    </PageLayout>
  );
});

export default DelegateCard;
