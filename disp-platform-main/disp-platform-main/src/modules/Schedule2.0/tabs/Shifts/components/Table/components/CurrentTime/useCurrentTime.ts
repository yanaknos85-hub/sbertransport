import { useEffect, useState } from 'react';

import moment from 'moment';

const useCurrentTime = () => {
  const [currentTime, setCurrentTime] = useState(moment());

  useEffect(() => {
    const updateTime = () => setCurrentTime(moment());

    const timer = setTimeout(updateTime, 60 * 1000);

    return () => {
      clearTimeout(timer);
    };
  }, [currentTime]);

  return currentTime;
};

export default useCurrentTime;
