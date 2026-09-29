import React, { FC, Suspense } from 'react';

import { Header } from 'modules/Schedule2.0/tabs/Shifts/components/Header/Header';
import { AnalyticsWorkloadProvider } from 'modules/Schedule2.0/tabs/Shifts/context/analyticsWorkload.context';
import { EstimatedHoursProvider } from 'modules/Schedule2.0/tabs/Shifts/context/estimatedHours.context';
import { SelectedTripProvider } from 'modules/Schedule2.0/tabs/Shifts/context/selectedTrip.context';
import { ShiftsQueryProvider } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';
import { TableZoomProvider } from 'modules/Schedule2.0/tabs/Shifts/context/tableZoom.context';

import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import { Table } from './components/Table/Table';
import { DeleteModal } from './modals/DeleteModal/DeleteModal';
import { FiltersModal } from './modals/FiltersModal/FiltersModal';
import { OrderModal } from './modals/OrderModal/OrderModal';
import { SettingsModal } from './modals/SettingsModal/SettingsModal';

const Shifts: FC = () => {
  return (
    <ShiftsQueryProvider>
      <SelectedTripProvider>
        <TableZoomProvider>
          <EstimatedHoursProvider>
            <AnalyticsWorkloadProvider>
              <Header />
              <ErrorBoundary>
                <Suspense fallback={<SpinWrapped />}>
                  <Table />

                  <DeleteModal />
                  <FiltersModal />
                  <OrderModal />
                  <SettingsModal />
                </Suspense>
              </ErrorBoundary>
            </AnalyticsWorkloadProvider>
          </EstimatedHoursProvider>
        </TableZoomProvider>
      </SelectedTripProvider>
    </ShiftsQueryProvider>
  );
};

export default Shifts;
