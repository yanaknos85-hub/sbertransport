import { PageHeader } from 'antd';
import React, { FC, useCallback } from 'react';
import { useHistory } from 'react-router-dom';

import { useEditApprovedTripRequest } from 'api/trip-requests';

import { EmptyRequest } from 'modules/EmployeeApp/shared/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import { useCurrentTripRequest } from 'shared/hooks/trip';

import EditTripRequestForm, { TripRequestFormValues } from '../EditTripRequestForm';

export const EditApprovedTripRequest: FC = () => {
  const { currentTripRequest } = useCurrentTripRequest();

  const history = useHistory();
  const goBack = useCallback(() => {
    history.push('./');
  }, [history]);

  const [editApprovedTripRequest, { isLoading }] = useEditApprovedTripRequest();

  const handleEditForm = (values: TripRequestFormValues): void => {
    if (!currentTripRequest) {
      return;
    }
    editApprovedTripRequest({
      data: values.expected,
      reqId: currentTripRequest.id,
    }).then(() => {
      goBack();
    });
  };

  return (
    <>
      {currentTripRequest === undefined && <SpinWrapped />}
      {currentTripRequest === null && <EmptyRequest back={goBack} />}
      {currentTripRequest?.isExisting && (
        <>
          <PageHeader title={`Редактировать ${currentTripRequest.humanReadableId}`} onBack={goBack} />
          <EditTripRequestForm
            onSubmit={handleEditForm}
            request={currentTripRequest}
            disabledFields={['date', 'purpose', 'coopTrip', 'passenger', 'preferences', 'personalCar']}
            submitDisabled={isLoading}
          />
        </>
      )}
    </>
  );
};
