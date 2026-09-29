import React, { FC } from 'react';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import { TripPurposeForm } from './TripPurposeForm';

const TripPurposeContainer: FC<{ handleClose?: () => void; id?: string }> = ({ handleClose, id }) => (
  <React.Suspense fallback={<SpinWrapped />}>
    <TripPurposeForm handleClose={handleClose} id={id} />
  </React.Suspense>
);

export default TripPurposeContainer;
