import React, { useMemo, useRef } from 'react';
import type { FC } from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import Panel from 'components/Panel';

import mfLoader from 'mf/MFLoader';

import styles from './contractors.module.scss';

const ContractorsProvider = React.lazy(() => mfLoader(import('platform/modules/TariffSettings/TariffSettingsProvider')));
const ContractorsContent = React.lazy(() => mfLoader(import('platform/modules/TariffSettings/Contractors')));

const Contractors: FC = () => {
  const footerRef = useRef<HTMLDivElement>(null);

  const context = useMemo(() => ({
    footer: footerRef,
  }), []);

  return (
    <>
      <Panel>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <ContractorsProvider value={context}>
              <ContractorsContent />
            </ContractorsProvider>
          </React.Suspense>
        </ErrorBoundary>
      </Panel>

      <div ref={footerRef} className={styles.footer} />
    </>
  );
};

export default Contractors;
