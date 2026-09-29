import React from 'react';

import { useGetRequestSuburbCompensation, useGetRequestTravelCardCompensation } from 'api/requests-with-compensation';

import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';

import uuid from 'utils/uuid';

import CompensationImageWrapper from './CompensationImageWrapper';

import styles from './styles.module.scss';

const CompensationInner = ({ request }: { request: TripRequestModel }): JSX.Element | null => {
  const { compensationType } = request;

  const defaultCompensationMethod = () => ({ data: undefined, isLoading: false });
  const requestCompensationMethod
    = (compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION && useGetRequestSuburbCompensation)
    || (compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION && useGetRequestTravelCardCompensation)
    || defaultCompensationMethod;

  const { data: requestCompensation, isLoading: requestIsLoading } = requestCompensationMethod(request.id);
  const documents = requestCompensation?.compensationDocuments;

  return (
    <div className={styles.compensationInner}>
      {(documents || []).map(document => (
        <CompensationImageWrapper
          key={uuid()}
          document={document}
          requestIsLoading={requestIsLoading}
        />
      ))}
    </div>
  );
};

export default CompensationInner;
