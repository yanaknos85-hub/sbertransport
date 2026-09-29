import { FC } from 'react';
import { PassTrip } from 'api/services/Trips/Trips.types';
import NavBar from 'components/NavBar';
import TripInfo from 'components/TripInfo';
import styles from './Detailed.module.scss';

interface DetailedProps {
  trip: PassTrip;
  close: () => void;
}

const Detailed: FC<DetailedProps> = ({ trip, close }) => {
  return (
    <div>
      <NavBar onBack={close}>Детали поездки</NavBar>

      <div className={styles.content}>
        <TripInfo trip={trip} />
      </div>
    </div>
  );
};

export default Detailed;
