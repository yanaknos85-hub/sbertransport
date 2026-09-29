
import { FormInstance } from 'antd/es/form/Form';
import React, { useState } from 'react';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { CoopTrip, FormValues } from '../../types/types';
import styles from './styles.module.scss';
import { SuitableTripView } from './SuitableTripView';

interface SuitableTripsViewProps {
  coopTripData: CoopTrip;
  form: FormInstance;
  onCommonFinish: (
    data: FormValues,
    isTripSearching?: boolean,
    isSuitabelTrip?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ) => void;
}

const SuitableTripsItem: React.FC<SuitableTripsViewProps> = ({
  coopTripData,
  form,
  onCommonFinish,
}: SuitableTripsViewProps): JSX.Element => {
  const [showAll, setShowAll] = useState(false);
  const toggleShowAll = () => {
    setShowAll(!showAll);
  };
  const { suitableCooperativeTrips } = coopTripData;
  const tripsToDisplay = showAll ? suitableCooperativeTrips : suitableCooperativeTrips.slice(0, 3);

  return (
    <div>
      {tripsToDisplay.map(item => (
        <SuitableTripView
          key={item.id}
          item={item}
          form={form}
          onCommonFinish={onCommonFinish}
        />
      ))}
      {suitableCooperativeTrips.length > 3 && (
        <div className={styles.buttonShowAll} onClick={toggleShowAll}>
          <span>{!showAll && 'Показать все'}</span>
        </div>
      )}
    </div>
  );
};

export default SuitableTripsItem;
