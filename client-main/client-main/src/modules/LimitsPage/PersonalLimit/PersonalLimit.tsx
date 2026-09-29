import React, { FC, useMemo } from 'react';

import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { RemainingFunds } from '../Components/RemainingFunds/RemainingFunds';
import { LimitBarList } from '../Components/LimitBarList/LimitBarList';
import { Container } from './PersonalLimit.styled';

interface PersonalLimitProps {
  sharing: LimitSharing[];
  restrict: string[];
}

export const PersonalLimit: FC<PersonalLimitProps> = ({ sharing, restrict }) => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const commonLimit = useMemo(() => {
    const result = { sum: 0, balance: 0 };

    sharing.forEach((limit: LimitSharing) => {
      result.sum = result.sum + (limit?.limitSharingPerPeriodDTO?.sum ?? 0);
      result.balance = result.balance + (limit?.limitSharingPerPeriodDTO?.balance ?? 0);
    });

    return { limitSharingPerPeriodDTO: { ...result } };
  }, [sharing]);

  const handleSendRequest = () => {
    limitsStore.refreshLimits();
  };

  return (
    <Container>
      <RemainingFunds
        title="Остаток личного лимита"
        limit={commonLimit as LimitSharing}
        limitType={LIMIT_TYPE.EMPLOYEE}
        restrict={restrict}
        isPersonal
      />
      <LimitBarList
        sharing={sharing}
        emptyText="На Вас не выделен персональный лимит"
        limitType={LIMIT_TYPE.EMPLOYEE}
        onSendRequest={handleSendRequest}
        restrict={restrict}
      />
    </Container>
  );
};
