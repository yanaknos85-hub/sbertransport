import React, { FC, Suspense } from 'react';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { AppTitles } from 'constants/app.constants';
import PageLayout from 'components/PageLayout/PageLayout';
import Panel from 'components/Panel/Panel';
import { SpinWrapped } from 'components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'components/ErrorBoundary/ErrorBoundary';

import { VehiclesDetailed } from './components/VehiclesDetailed/VehiclesDetailed';
import { TransportDetailed } from './components/TransportDetailed/TransportDetailed';
import { TransportList } from './components/List/TransportList';
import { VehiclesList } from './components/List/VehiclesList';
import { ModalFormVehicles } from './components/ModalFormVehicles/ModalFormVehicles';
import ModalFormTransport from './components/ModalFormTransport/ModalFormTransport';
import TransportModalDelete from './components/TransportModalDelete/TransportModalDelete';
import TransportEditModal from './components/TransportEditModal/TransportEditModal';
import { UploadModal } from './components/UploadModal/UploadModal';
import { ResultModal } from './components/ResultModal/ResultModal';

import { ActiveVehicleProvider } from './context/ActiveVehicle';
import { ActiveTransportProvider } from './context/ActiveTransport';
import { ModalFormProvider } from './context/ModalForm';
import { EditDeleteProvider } from './context/EditDeleteContext';

const Vehicles: FC = () => {
  const { isInternal } = useSelfAutopark().data;

  return (
    <PageLayout>
      <ActiveVehicleProvider>
        <ActiveTransportProvider>
          <ModalFormProvider>
            <EditDeleteProvider>
              <Panel title={AppTitles.Vehicles} contentWithoutPadding>
                <ErrorBoundary>
                  <Suspense fallback={<SpinWrapped />}>
                    {isInternal ? <TransportList /> : <VehiclesList />}
                  </Suspense>
                </ErrorBoundary>
              </Panel>

              <Panel>
                <ErrorBoundary>
                  <Suspense fallback={<SpinWrapped />}>
                    {isInternal ? <TransportDetailed /> : <VehiclesDetailed />}
                  </Suspense>
                </ErrorBoundary>
              </Panel>
              <ModalFormVehicles />
              <ModalFormTransport />
              <UploadModal />
              <ResultModal />
              <TransportModalDelete />
              <TransportEditModal />
            </EditDeleteProvider>
          </ModalFormProvider>
        </ActiveTransportProvider>
      </ActiveVehicleProvider>
    </PageLayout>
  );
};

export default Vehicles;
