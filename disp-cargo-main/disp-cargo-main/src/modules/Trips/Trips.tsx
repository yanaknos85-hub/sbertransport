import React, { FC, useRef } from 'react';
import { useClickOutside } from 'hooks/useClickOutside';
import { TripsModalProvider, useTripsModal } from './context/TripsModal';
import { Filters } from './components/Filters';
import { Map } from './components/Map';
import { TripsTable } from './components/TripsTable';
import { SetDriverModal } from './components/SetDriverModal';
import { SetTripModal } from './components/SetTripsModal';
import { EditModal } from './components/EditModal';
import { AddressModal } from './components/Addresses/Addresses';
import { CommentModal } from './components/Comment/Comment';
import { ActiveTripProvider, useActiveTrip } from './context/ActiveTrip';
import { TripsQueryProvider } from './context/TripsQuery';
import useMyTrips from './hooks/useMyTrips';
import { TripsSettingsProvider, useTripsSettings } from './context/TripsSettings.context';

import styles from './styles.module.scss';

const TripsWrapper: FC = ({ children }) => {
  const { activeTrip, setActiveTrip } = useActiveTrip();
  const { isOpened } = useTripsModal();

  const requestsRef = useRef<HTMLDivElement | null>(null);

  const clearActiveTrip = () => setActiveTrip(undefined);
  useClickOutside(requestsRef, clearActiveTrip, !!activeTrip && !isOpened());

  return (
    <div ref={requestsRef} className={styles.requestWrapper}>
      {children}
    </div>
  );
};

const Trips: FC = () => {
  const { map, table } = useTripsSettings().settings;
  const [isSelfTripsVisible, setIsSelfTripsVisible] = useMyTrips();

  return (
    <>
      <Filters isSelfTripsVisible={isSelfTripsVisible} setIsSelfTripsVisible={setIsSelfTripsVisible} />

      <ActiveTripProvider>
        <TripsWrapper>
          {map.isVisible && <Map />}

          {table.isVisible && <TripsTable isSelfTripsVisible={isSelfTripsVisible} />}
        </TripsWrapper>

        <SetDriverModal />
        <SetTripModal />
        <EditModal />
        <AddressModal />
        <CommentModal />
      </ActiveTripProvider>
    </>
  );
};

const TripsWithProviders: FC = () => (
  <TripsSettingsProvider>
    <TripsQueryProvider>
      <TripsModalProvider>
        <Trips />
      </TripsModalProvider>
    </TripsQueryProvider>
  </TripsSettingsProvider>

);

export default TripsWithProviders;
