import { useMemo } from 'react';
import { useTripsModal } from '../../context/TripsModal';

export const useValues = () => {
  const { modalState } = useTripsModal();

  const defaultValues = useMemo(
    () => ({
      ...modalState.trip,
      driverWaitingTime: modalState.trip?.driverWaitingTime
        ? Math.round(modalState.trip?.driverWaitingTime / 60)
        : null,
      driverId: modalState.trip?.driver?.id,
      planningShiftId: modalState.trip?.planned?.driver.shiftId,
    }),
    [modalState.trip]
  );

  return { defaultValues };
};
