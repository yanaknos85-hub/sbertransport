import React, { useEffect, useState } from 'react';

const IS_SELF_TRIPS_CARGO_VISIBLE = 'IS_SELF_TRIPS_CARGO_VISIBLE';

const useMyTrips = (): [boolean, React.Dispatch<React.SetStateAction<boolean>>] => {
  const [isSelfTripsVisible, setIsSelfTripsVisible] = useState(localStorage.getItem(IS_SELF_TRIPS_CARGO_VISIBLE) === 'true');

  useEffect(() => {
    localStorage.setItem(IS_SELF_TRIPS_CARGO_VISIBLE, String(isSelfTripsVisible));
  }, [isSelfTripsVisible]);

  return [isSelfTripsVisible, setIsSelfTripsVisible];
};

export default useMyTrips;
