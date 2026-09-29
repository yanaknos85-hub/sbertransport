import React, { FC, useMemo } from 'react';

import { LIMIT_TYPE, LimitResponsible, LimitSharing } from 'stores/Limits/Limit.interface';
import { LimitBarList } from '../Components/LimitBarList/LimitBarList';
import { RemainingFunds } from '../Components/RemainingFunds/RemainingFunds';
import { Responsibles } from '../Components/Responsible/Responsibles';

import { Container, Header } from './DepartmentLimit.styled';

interface DepartmentLimitProps {
  sharing: LimitSharing[];
  isDepartmentHead: boolean;
  responsibles: LimitResponsible[];
  onChangeServiceType?: (type: string) => void;
}

export const DepartmentLimit: FC<DepartmentLimitProps> = ({
  sharing, isDepartmentHead, responsibles, onChangeServiceType,
}) => {
  const commonLimit = useMemo(() => {
    const result = { sum: 0, balance: 0 };

    sharing.forEach((limit: LimitSharing) => {
      result.sum = result.sum + (limit?.limitSharingPerPeriodDTO?.sum ?? 0);
      result.balance = result.balance + (limit?.limitSharingPerPeriodDTO?.balance ?? 0);
    });

    return { limitSharingPerPeriodDTO: { ...result } };
  }, [sharing]);

  return (
    <Container>
      <Header>
        <RemainingFunds
          title="Остаток лимита подразделения"
          limit={commonLimit as LimitSharing}
          limitType={LIMIT_TYPE.DEPARTMENT}
          isDepartmentHead={isDepartmentHead}
        />
        <Responsibles responsibles={responsibles} />
      </Header>
      <LimitBarList
        sharing={sharing}
        emptyText="На ваше подразделение лимит не выделен"
        limitType={LIMIT_TYPE.DEPARTMENT}
        isDepartmentHead={isDepartmentHead}
        onChangeServiceType={onChangeServiceType}
      />
    </Container>
  );
};
