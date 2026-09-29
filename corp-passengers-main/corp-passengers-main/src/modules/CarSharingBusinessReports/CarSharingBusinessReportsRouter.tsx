import React from 'react';
import moment from 'moment';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { RegistryFiltersProvider } from '../Registry/RegistryFilterContext';
import { CarSharingReportFilters } from 'stores/CarSharingTrip/CarSharingTrip.interface';

import CarSharingBusinessReports from './CarSharingBusinessReports';

export const CarSharingBusinessReportsRouter = withErrorBoundary(() => {
  const defaultFilters = {
    desiredDate: {
      start: moment().startOf('month').format('YYYY-MM-DDTHH:mm:ss'),
      end: moment().format('YYYY-MM-DDTHH:mm:ss'),
    },
  } as CarSharingReportFilters;

  return (
    <RegistryFiltersProvider<CarSharingReportFilters> defaultFilters={defaultFilters}>
      <CarSharingBusinessReports />
    </RegistryFiltersProvider>
  );
});

export default CarSharingBusinessReportsRouter;
