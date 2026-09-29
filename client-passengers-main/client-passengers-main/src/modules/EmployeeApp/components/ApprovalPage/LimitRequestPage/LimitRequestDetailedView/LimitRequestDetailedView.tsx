import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { EmptyRequest } from 'modules/EmployeeApp/shared/EmptyFactory';

import { StoreNames } from 'stores/StoreNames.enum';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import LimitRequestContent from './LimitRequestContent';
import { LimitRequestButtons } from './LimitRequestDetailedComponents/LimitRequestButtons';
import { useLimitRequestDetailedView } from './useLimitRequestDetailedView';

const LimitRequestDetailedView: FC = observer(() => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  useEffect(() => {
    limitsStore.getLimitRequest();
  }, [limitsStore]);

  const match = useRouteMatch<any>();
  const { reqId } = match.params;

  const request = match.url.includes('closed')
    ? limitsStore.allLimitRequests.find(x => x.id === reqId)
    : limitsStore.limitRequests.find(x => x.id === reqId);

  const { goToList } = useLimitRequestDetailedView();

  return (
    <PageLayout
      title="Заявка на пополнение лимита"
      onBack={goToList}
      empty={<EmptyRequest back={goToList} />}
      loading={request === undefined}
    >
      {request && (
        <>
          <LimitRequestContent request={request} />
          <LimitRequestButtons request={request} />
        </>
      )}
    </PageLayout>
  );
});

export default LimitRequestDetailedView;
