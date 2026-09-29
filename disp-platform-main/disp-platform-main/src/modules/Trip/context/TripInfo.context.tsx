import React, {
  createContext, FC, useContext, useMemo
} from 'react';
import { useProfile } from 'api/profile/profile.api';
import { useCheckinInfoPass, usePassTrip } from 'api/trips/trips.api';
import { UUID } from 'utils/io-ts';
import { ITripInfo } from '../types/tripPass.interface';
import { useWaypoints } from '../hooks/useWaypoints';

export const useTrip = (tripId: UUID): ITripInfo => {
  const { contractorId } = useProfile().data;

  const trip = usePassTrip({ contractorId, tripId }).data;
  const checkinInfo = useCheckinInfoPass({ contractorId, tripId }, { suspense: false }).data;

  // Собираем точки, определяем тип по информации о чекинах
  const { waypoints } = useWaypoints(trip, checkinInfo);

  return useMemo(() => ({
    trip,
    checkinInfo,
    waypoints,
  }), [checkinInfo, trip, waypoints]);
};

const TripInfoContext = createContext<ITripInfo>({} as ITripInfo);

/**
 * Хук для получения полной информации о поездке
 * @returns Объект
 * {@link ITripInfo}
*/
export const useTripInfo = () => useContext(TripInfoContext);

export const TripInfoProvider: FC<{ tripId: UUID }> = ({
  children,
  tripId,
}) => {
  const tripInfo = useTrip(tripId);

  return (
    <TripInfoContext.Provider value={tripInfo}>
      {children}
    </TripInfoContext.Provider>
  );
};

