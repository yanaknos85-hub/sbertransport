import React, { FC, Suspense } from 'react';

import { AppTitles } from 'constants/app.constants';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import PageLayout from 'components/PageLayout/PageLayout';
import Panel from 'components/Panel/Panel';

import { DispatcherList } from './components/DispatcherList/DispatcherList';
import { DispatcherDetailed } from './components/DispatcherDetailed/DispatcherDetailed';
import { ModalFormDispatcher } from './components/ModalFormDispatcher/ModalFormDispatcher';
import { ModalFormProvider } from './context/ModalForm';
import { ActiveDispatcherProvider } from './context/ActiveDispatcher';
import { EditDeleteProvider } from './context/EditDeleteContext';

const Dispatchers: FC = () => (
  <PageLayout>
    <ActiveDispatcherProvider>
      <ModalFormProvider>
        <EditDeleteProvider>
          <Panel title={AppTitles.Dispatchers} contentWithoutPadding>
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                <DispatcherList />
              </Suspense>
            </ErrorBoundary>
          </Panel>

          <Panel>
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                <DispatcherDetailed />
              </Suspense>
            </ErrorBoundary>
          </Panel>
          <ModalFormDispatcher />
        </EditDeleteProvider>
      </ModalFormProvider>
    </ActiveDispatcherProvider>
  </PageLayout>
);

export default Dispatchers;
