import { useMemo } from 'react';
import { useTripsModal } from '../../context/TripsModal';

export const useValues = () => {
  const { modalState } = useTripsModal();

  const defaultValues = useMemo(
    () => ({
      driverId: modalState.trip?.driver?.id,
      planningShiftId: modalState.trip?.planned?.driver.shiftId,
    }),
    [modalState.trip]
  );

  return { defaultValues };
};
