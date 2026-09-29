import { useMemo } from 'react';
import { useTripsModal } from '../../context/TripsModal';
import moment from 'moment';
import { convertToRubles } from 'utils/convertToRubles';

export const useValues = () => {
  const { modalState } = useTripsModal();

  const defaultValues = useMemo(
    () => ({
      ...modalState.trip,
      loadersWorkTime: modalState.trip?.loadersWorkTime
        ? Math.round(modalState.trip?.loadersWorkTime / 1000 / 60)
        : undefined,
      driverId: modalState.trip?.driver?.id,
      dispatcherStartTime: moment.parseZone(modalState.trip?.dispatcherStartTime) || undefined,
      loaders: modalState.trip?.loaders || undefined,
      driverWaitingTime: modalState.trip?.driverWaitingTime
        ? Math.round(modalState.trip?.driverWaitingTime / 1000 / 60)
        : undefined,
      factCost: modalState.trip?.factCost ? convertToRubles(modalState.trip?.factCost) : undefined,
      finishTime: moment.parseZone(modalState.trip?.finishTime) || undefined,
    }),
    [modalState.trip]
  );

  return { defaultValues };
};
