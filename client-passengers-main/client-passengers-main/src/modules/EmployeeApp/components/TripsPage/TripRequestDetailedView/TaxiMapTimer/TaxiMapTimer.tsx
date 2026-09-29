import React, { useState, useEffect, useRef } from 'react';
import type { FC } from 'react';

import { TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';
import styles from './styles.module.scss';

interface Props {
  status: TripStatusesEnum;
  duration: number;
}

const FREE_WAITING_TIME = 300;

const TaxiMapTimer: FC<Props> = ({ status, duration }) => {
  const [timer, setTimer] = useState<number | undefined>(FREE_WAITING_TIME);
  const intervalId = useRef<NodeJS.Timeout>();

  const taxiMapTimerText = status === TripStatusesEnum.TAXI_DRIVER_ON_THE_WAY ? 'Подача через'
    : 'Бесплатное время ожидания:';
  const timeText = status === TripStatusesEnum.TAXI_DRIVER_ON_THE_WAY ? `~${Math.ceil(duration / 60)} мин`
    : timer ? `${Math.floor(timer / 60)}:${String(timer % 60).padStart(2, '0')}` : undefined;

  useEffect(() => {
    if (status === TripStatusesEnum.TAXI_DRIVER_ARRIVED) {
      intervalId.current = setInterval(
        () => setTimer(timer => timer ? timer - 1 : 0),
        1000
      );
      return () => clearInterval(intervalId.current!);
    }
  }, [status]);

  useEffect(() => {
    if (timer === 0) {
      clearInterval(intervalId.current!);
      setTimer(undefined);
    }
  }, [timer]);

  return (status === TripStatusesEnum.TAXI_DRIVER_ARRIVED && !!timer)
    || status === TripStatusesEnum.TAXI_DRIVER_ON_THE_WAY ? (
      <div className={styles.timer}>
        <p>{`${taxiMapTimerText} ${timeText}`}</p>
      </div>
    ) : null;
};

export default TaxiMapTimer;
