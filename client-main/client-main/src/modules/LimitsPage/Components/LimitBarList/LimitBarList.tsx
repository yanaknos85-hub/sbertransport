import React from 'react';
import { Tabs } from 'antd';

import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { LIMIT_SERVICE_TYPE, LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { toRubles } from 'utils';
import { UUID } from 'utils/io-ts';
import { getAvailablePercentage } from 'utils/trips';
import { LimitBarCard } from '../LimitBarCard/LimitBarCard';
import { StyledList, StyledListItem } from './LimitBarList.styled';
import { TabsStyled } from 'modules/LimitsPage/LimitsPage.styled';

const CARGO_LIST = ['COURIER', 'DEDICATED', 'DOMESTIC_COURIER', 'INDIVIDUAL', 'INTERREGIONAL'];

interface LimitBarListProps {
  emptyText: string;
  sharing: LimitSharing[];
  limitType: LIMIT_TYPE;
  isDepartmentHead?: boolean;
  restrict?: string[];
  onSendRequest?: () => void;
  onClick?: (id: UUID) => void;
  onChangeServiceType?: (type: LIMIT_SERVICE_TYPE) => void;
}

export const LimitBarList: React.FC<LimitBarListProps> = ({
  emptyText,
  sharing,
  limitType,
  isDepartmentHead,
  restrict,
  onSendRequest,
  onClick,
  onChangeServiceType,
}) => {
  const sharingCargo = [];
  const sharingPassengers = [];

  sharing.forEach(item => {
    if (CARGO_LIST.includes(item.transportType)) {
      sharingCargo.push(item);
    } else {
      sharingPassengers.push(item);
    }
  });

  return (
    <TabsStyled
      onChange={onChangeServiceType}
    >
      <Tabs.TabPane
        tab="Пассажирские перевозки"
        key={LIMIT_SERVICE_TYPE.PASSENGER}
      >
        <StyledList
          dataSource={sharingPassengers.sort((a, b) => a.transportType.localeCompare(b.transportType)) ?? []}
          locale={{ emptyText: <EmptyDataList title={emptyText} /> }}
          renderItem={(limit: LimitSharing): JSX.Element => (
            <StyledListItem>
              <LimitBarCard
                key={limit.id}
                limitType={limitType}
                percent={getAvailablePercentage(limit)}
                balance={toRubles(limit?.limitSharingPerPeriodDTO?.balance ?? 0)}
                sum={toRubles(limit?.limitSharingPerPeriodDTO?.sum ?? 0)}
                transportType={limit.transportType}
                isDepartmentHead={isDepartmentHead}
                restrict={restrict}
                onClick={() => onClick(limit.id)}
                onSendRequest={onSendRequest}
              />
            </StyledListItem>
          )}
        />
      </Tabs.TabPane>
      <Tabs.TabPane tab="Грузовые перевозки" key={LIMIT_SERVICE_TYPE.CARGO}>
        <StyledList
          dataSource={sharingCargo.sort((a, b) => a.transportType.localeCompare(b.transportType)) ?? []}
          locale={{ emptyText: <EmptyDataList title={emptyText} /> }}
          renderItem={(limit: LimitSharing): JSX.Element => (
            <StyledListItem>
              <LimitBarCard
                key={limit.id}
                limitType={limitType}
                percent={getAvailablePercentage(limit)}
                balance={toRubles(limit?.limitSharingPerPeriodDTO?.balance ?? 0)}
                sum={toRubles(limit?.limitSharingPerPeriodDTO?.sum ?? 0)}
                transportType={limit.transportType}
                isDepartmentHead={isDepartmentHead}
                restrict={restrict}
                onClick={() => onClick(limit.id)}
                onSendRequest={onSendRequest}
              />
            </StyledListItem>
          )}
        />
      </Tabs.TabPane>
    </TabsStyled>
  );
};
