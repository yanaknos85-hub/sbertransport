import React from 'react';
import mfLoader from 'mf/MFLoader';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useTariffSettingsContext } from '../../context/TariffSettings.context';

const TariffSettingsProvider = React.lazy(() => mfLoader(import('platform/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettings = React.lazy(() => mfLoader(import('platform/modules/TariffSettings/Contractors')));

const Contractors = () => {
  const tariffSettingsContext = useTariffSettingsContext();

  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SpinWrapped />}>
        <TariffSettingsProvider value={tariffSettingsContext}>
          <TariffSettings />
        </TariffSettingsProvider>
      </React.Suspense>
    </ErrorBoundary>
  );
};
export default Contractors;

