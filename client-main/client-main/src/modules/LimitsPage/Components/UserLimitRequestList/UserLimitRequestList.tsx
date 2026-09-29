import React, { FC } from 'react';

import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { ISpentActionsType } from '../../useDetailedLimitsMapper';
import { ListStyled } from './UserLimitRequestList.styled';
import { UserLimitRequestItem } from '../UserLimitRequestItem/UserLimitRequestItem';

const UserLimitRequestList: FC<{ dataSource: ISpentActionsType[] }> = ({ dataSource }) => (
  <ListStyled
    dataSource={dataSource}
    locale={{ emptyText: <EmptyDataList title="У вас нет заявок для согласования" /> }}
    renderItem={(request: ISpentActionsType) => <UserLimitRequestItem request={request} />}
  />
);

export default UserLimitRequestList;
