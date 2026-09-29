import React, { memo } from 'react';
import { TripRequestModel } from 'stores/Trip/models';

import CompensationInner from './CompensationInner';

interface CompensationDocumentProps {
  request: TripRequestModel;
}

const compensationDocumentPropsAreEqual = (
  prevProps: CompensationDocumentProps,
  nextProps: CompensationDocumentProps
) => prevProps.request.compensationType === nextProps.request.compensationType;

const CompensationDocument = memo(
  ({ request }: CompensationDocumentProps): JSX.Element | null => <CompensationInner request={request} />,
  compensationDocumentPropsAreEqual
);

export default CompensationDocument;
