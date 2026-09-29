import { DriversLocationWebsocket, CargoTrip, Waypoint } from 'api/trips-cargo/trips-cargo.types';
import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { UUID } from 'utils/io-ts';

type ModalType = 'setDriver' | 'setRequest' | 'editTrip' | 'filters' | 'download' | 'mutualSettlements' | 'addresses' | 'comment' | 'request';

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
      comments?: undefined;
    }
    | {
      type: 'setDriver';
      trip: CargoTrip;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
    | {
      type: 'editTrip';
      trip: CargoTrip;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
    | {
      type: 'addresses';
      trip?: CargoTrip;
      driverData?: undefined;
      waypoints?: Waypoint[];
      comments?: undefined;
    }
    | {
      type: 'comment';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: string[];
    }
    | {
      type: 'filters';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
    | {
      type: 'download';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
    | {
      type: 'mutualSettlements';
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
    | {
      type: null;
      trip?: undefined;
      driverData?: undefined;
      waypoints?: undefined;
      comments?: undefined;
    }
  >({ type: null });

  const isOpened = useCallback((type?: ModalType) => {
    return (!!type && modalState.type === type) || (!type && !!modalState.type);
  }, [modalState]);

  const openSetDriver = useCallback((trip: CargoTrip) => {
    setModalState({ type: 'setDriver', trip });
  }, []);

  const openSetRequest = useCallback((driverData: SetRequestData) => {
    setModalState({ type: 'setRequest', driverData });
  }, []);

  const openEdit = useCallback((trip: CargoTrip) => {
    setModalState({ type: 'editTrip', trip });
  }, []);

  const openAddresses = useCallback((waypoints: Waypoint[]) => {
    setModalState({ type: 'addresses', waypoints });
  }, []);

  const openComment = useCallback((comments: string[]) => {
    setModalState({ type: 'comment', comments });
  }, []);

  const openFilters = useCallback(() => {
    setModalState({ type: 'filters' });
  }, []);

  const openDownload = useCallback(() => {
    setModalState({ type: 'download' });
  }, []);

  const openMutualSettlements = useCallback(() => {
    setModalState({ type: 'mutualSettlements' });
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
    openComment,
    openFilters,
    openDownload,
    openMutualSettlements,
    closeModal,
    isOpened,
  };
};

export const [useTripsModal, TripsModalProvider] = createCallableCtx(useHook, { name: 'TripsModalProvider' });
