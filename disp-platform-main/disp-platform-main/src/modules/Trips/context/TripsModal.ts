import { DriversLocationWebsocket, PassTrip, Waypoint } from 'api/trips/trips.types';
import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { UUID } from 'utils/io-ts';

type ModalType = 'setDriver' | 'setRequest' | 'editTrip' | 'filters' | 'download' | 'addresses';

interface SetRequestData {
  driverId: DriversLocationWebsocket['id'];
  shiftId: UUID;
}

const useHook = () => {
  const [modalState, setModalState] = useState<
    | {
      type: 'setRequest';
      driverData?: SetRequestData;
      trip?: undefined;
      waypoints?: undefined;
    }
    | {
      type: 'setDriver';
      trip: PassTrip;
      driverData?: undefined;
      waypoints?: undefined;
    }
    | {
      type: 'editTrip';
      trip: PassTrip;
      driverData?: undefined;
      waypoints?: undefined;
    }
    | {
      type: 'addresses';
      trip?: PassTrip;
      driverData?: undefined;
      waypoints?: Waypoint[];
    }
    | {
      type: 'filters';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
    }
    | {
      type: 'download';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
    }
    | {
      type: null;
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
    }
  >({ type: null });

  const isOpened = useCallback((type?: ModalType) => {
    return (!!type && modalState.type === type) || (!type && !!modalState.type);
  }, [modalState]);

  const openSetDriver = useCallback((trip: PassTrip) => {
    setModalState({ type: 'setDriver', trip });
  }, []);

  const openSetRequest = useCallback((driverData: SetRequestData) => {
    setModalState({ type: 'setRequest', driverData });
  }, []);

  const openEdit = useCallback((trip: PassTrip) => {
    setModalState({ type: 'editTrip', trip });
  }, []);

  const openAddresses = useCallback((waypoints: Waypoint[]) => {
    setModalState({ type: 'addresses', waypoints });
  }, []);

  const openFilters = useCallback(() => {
    setModalState({ type: 'filters' });
  }, []);

  const openDownload = useCallback(() => {
    setModalState({ type: 'download' });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({ type: null });
  }, []);

  return {
    modalState,
    openSetDriver,
    openSetRequest,
    openEdit,
    openAddresses,
    openFilters,
    openDownload,
    closeModal,
    isOpened,
  };
};

export const [useTripsModal, TripsModalProvider] = createCallableCtx(useHook, { name: 'TripsModalProvider' });
