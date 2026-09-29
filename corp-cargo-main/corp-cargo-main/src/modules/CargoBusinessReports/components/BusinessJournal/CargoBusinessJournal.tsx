import React from 'react';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { CargoBusinessReportsTable } from '../PublicBusinessReportsTable/CargoBusinessReportsTable';

export const CargoBusinessJournal = withErrorBoundary(() => {
  return <CargoBusinessReportsTable />;
});
