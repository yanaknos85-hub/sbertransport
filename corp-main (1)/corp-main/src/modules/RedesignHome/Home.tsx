import React, { FC, Suspense } from 'react';
import { observer } from 'mobx-react';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';

import { TransportTypesProvider } from './context/TransportTypes.context';
import { HotButtonsProvider } from './context/HotButtons.context';

import { Service } from './components/Service/Service';
import { HotButtons } from './components/HotButtons/HotButtons';
import { WidgetsProvider } from './context/Widgets.context';
import { Widgets } from './components/Widgets/Widgets';
import { useOrganizationsGroups } from 'api/organizations/organizations-groups';

export const Home: FC = observer(() => {
  const { content: orgGroups } = useOrganizationsGroups().data;

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <TransportTypesProvider>
          <Service />
        </TransportTypesProvider>

        <HotButtonsProvider>
          <HotButtons />
        </HotButtonsProvider>

        <WidgetsProvider>
          <Widgets orgGroups={orgGroups} />
        </WidgetsProvider>

      </Suspense>
    </ErrorBoundary>
  );
});

export default Home;
