import React, { FC, useRef, useMemo } from 'react';

import { useTranslation } from 'i18n';
import { TRIPS_TABLE_SETTINGS } from 'api/trips/trips.constants';
import { Columns, staticColumns } from './constants';
import { useClickOutside } from 'hooks/useClickOutside';
import { useTableSettings } from 'hooks/useTableSettings';
import useMyTrips from './hooks/useMyTrips';

import { Filters } from './components/Filters';
import { Map } from './components/Map';
import { TripsTable } from './components/TripsTable';
import { SetDriverModal } from './components/SetDriverModal';
import { SetTripModal } from './components/SetTripsModal';
import { EditModal } from './components/EditModal';
import { AddressModal } from './components/Addresses/Addresses';

import { TripsModalProvider, useTripsModal } from './context/TripsModal';
import { ActiveTripProvider, useActiveTrip } from './context/ActiveTrip';
import { TripsQueryProvider } from './context/TripsQuery';
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
  const { t } = useTranslation();
  const { map, table } = useTripsSettings().settings;
  const [isSelfTripsVisible, setIsSelfTripsVisible] = useMyTrips();

  const columns = useMemo(
    () => Object.fromEntries(
      Object.values(Columns)
        .filter(column => !staticColumns.includes(column))
        .map(column => [column, t.Requests.Columns[column[0].toUpperCase() + column.slice(1)] ?? column])
    ),
    [t]
  );

  const {
    tableSettings, onSaveTableSettings, getOptimizeColumns,
  } = useTableSettings(columns, TRIPS_TABLE_SETTINGS);

  return (
    <>
      <Filters
        columns={columns}
        tableSettings={tableSettings}
        isSelfTripsVisible={isSelfTripsVisible}
        onSaveTableSettings={onSaveTableSettings}
        setIsSelfTripsVisible={setIsSelfTripsVisible}
      />

      <ActiveTripProvider>
        <TripsWrapper>
          {map.isVisible && <Map />}

          {table.isVisible && (
            <TripsTable
              isSelfTripsVisible={isSelfTripsVisible}
              getOptimizeColumns={getOptimizeColumns}
            />
          )}
        </TripsWrapper>

        <SetDriverModal />
        <SetTripModal />
        <EditModal />
        <AddressModal />
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
