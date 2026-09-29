import { PageHeader } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import { useHistory } from 'react-router-dom';

import { EmptyRequest } from 'modules/EmployeeApp/shared/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useRouteParamSub } from 'shared/hooks/useRouteParamSub';
import { StoreNames } from 'stores/StoreNames.enum';

import CreateTripRequest from './CreateTripRequest';

export const EditTripRequestComponent: FC = observer(() => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const [reqId] = useRouteParamSub('reqId');
  const history = useHistory();

  useEffect(() => {
    if (reqId && !tripStore.currentTripRequestId) {
      tripStore.setCurrentTripRequest(reqId);
    }
    return (): void => tripStore.clearCurrentRequest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reqId, tripStore.currentTripRequestId]);

  const goBack = (): void => history.goBack();

  return (
    <>
      {tripStore.currentTripRequest === undefined && <SpinWrapped />}
      {tripStore.currentTripRequest === null && <EmptyRequest back={goBack} />}
      {tripStore.currentTripRequest?.isExisting && (
        <>
          <PageHeader title={`Редактировать ${tripStore.currentTripRequest.humanReadableId}`} onBack={goBack} />
          <CreateTripRequest />
        </>
      )}
    </>
  );
});
