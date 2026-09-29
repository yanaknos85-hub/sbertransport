import React, { FC, Suspense } from 'react';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';

import { TransportTypesProvider } from './context/TransportTypes.context';
import { HotButtonsProvider } from './context/HotButtons.context';

import { Service } from './components/Service/Service';
import { HotButtons } from './components/HotButtons/HotButtons';
import { WidgetsProvider } from './context/Widgets.context';
import { Widgets } from './components/Widgets/Widgets';

export const Home: FC = () => (
  <ErrorBoundary>
    <Suspense fallback={<SpinWrapped />}>
      <TransportTypesProvider>
        <Service />
      </TransportTypesProvider>

      <HotButtonsProvider>
        <HotButtons />
      </HotButtonsProvider>

      <WidgetsProvider>
        <Widgets />
      </WidgetsProvider>
    </Suspense>
  </ErrorBoundary>
);

export default Home;
