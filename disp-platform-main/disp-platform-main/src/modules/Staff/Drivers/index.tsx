import React, { FC, Suspense } from 'react';

import { AppTitles } from 'constants/app.constants';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'components/ErrorBoundary';
import PageLayout from 'components/PageLayout/PageLayout';
import Panel from 'components/Panel/Panel';

import { DriverList } from './components/DriverList/DriverList';
import { DriversDetailed } from './components/DriversDetailed/DriversDetailed';
import { ModalFormDriver } from './components/ModalFormDriver/ModalFormDriver';
import { ActiveDriverProvider } from './context/ActiveDriver';
import { ModalFormProvider } from './context/ModalForm';
import { EditDeleteProvider } from './context/EditDeleteContext';

const Drivers: FC = () => (
  <PageLayout>
    <ActiveDriverProvider>
      <ModalFormProvider>
        <EditDeleteProvider>
          <Panel title={AppTitles.Drivers} contentWithoutPadding>
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                <DriverList />
              </Suspense>
            </ErrorBoundary>
          </Panel>

          <Panel>
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                <DriversDetailed />
              </Suspense>
            </ErrorBoundary>
          </Panel>
          <ModalFormDriver />
        </EditDeleteProvider>
      </ModalFormProvider>
    </ActiveDriverProvider>
  </PageLayout>
);

export default Drivers;
