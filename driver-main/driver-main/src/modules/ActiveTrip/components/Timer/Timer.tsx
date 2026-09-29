import { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { useAppStore } from 'stores/stores.context';
import styles from './Timer.module.scss';

const Timer: FC = observer(() => {
  const { activeTripStore } = useAppStore();

  useEffect(() => {
    activeTripStore.startWaitTimer();

    return () => {
      activeTripStore.stopWaitTimer();
    };
  }, [activeTripStore]);

  return (
    <div className={styles.timer}>
      {activeTripStore.waitTimer.format(`${activeTripStore.waitTimer.hours() > 0 ? 'HH:' : ''}mm:ss`)}
    </div>
  );
});

export default Timer;
