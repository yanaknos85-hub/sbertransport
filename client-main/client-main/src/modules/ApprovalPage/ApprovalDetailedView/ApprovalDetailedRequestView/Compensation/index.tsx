import React from 'react';

import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';

import CompensationInner from './CompensationInner';

interface CompensationDocumentProps {
  request: TripRequestModel;
}

const compensationDocumentPropsAreEqual = (
  prevProps: CompensationDocumentProps,
  nextProps: CompensationDocumentProps
) => prevProps.request.compensationType === nextProps.request.compensationType;

const CompensationDocument = React.memo(({ request }: CompensationDocumentProps): JSX.Element | null => {
  const { compensationType } = request;
  const disableCompensationImage = compensationType === TransportCompensations.CITY_TRIP_COMPENSATION;

  return disableCompensationImage ? null : <CompensationInner request={request} />;
}, compensationDocumentPropsAreEqual);

export default CompensationDocument;
