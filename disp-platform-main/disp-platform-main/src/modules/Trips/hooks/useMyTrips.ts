import { useRoleMap } from 'hooks/useRoleMap';
import React, { useEffect, useState } from 'react';

const IS_SELF_TRIPS_PASS_VISIBLE = 'IS_SELF_TRIPS_PASS_VISIBLE';

const useMyTrips = (): [boolean, React.Dispatch<React.SetStateAction<boolean>>] => {
  const { isAdmin } = useRoleMap();

  const [isSelfTripsVisible, setIsSelfTripsVisible] = useState(isAdmin ? false : localStorage.getItem(IS_SELF_TRIPS_PASS_VISIBLE) === 'true');

  useEffect(() => {
    localStorage.setItem(IS_SELF_TRIPS_PASS_VISIBLE, String(isSelfTripsVisible));
  }, [isSelfTripsVisible]);

  return [isSelfTripsVisible, setIsSelfTripsVisible];
};

export default useMyTrips;
