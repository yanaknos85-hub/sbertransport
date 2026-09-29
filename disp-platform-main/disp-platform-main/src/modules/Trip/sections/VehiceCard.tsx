import React, { FC } from 'react';
import { PassTrip } from 'api/trips/trips.types';
import Panel from 'components/Panel/Panel';
import withErrorBoundary from 'components/withErrorBoundary';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import { getFullVehicle } from 'utils/getFullVehicle';

import PersonalCard from '../components/PersonalCard/PersonalCard';
import Taxi from 'assets/images/taxi.png';

/** Автомобиль */
export const VehicleCard: FC<{ vehicle: PassTrip['vehicle'] }> = withErrorBoundary(({ vehicle }) => (
  <Panel
    fullHeight
    title="Автомобиль"
    smallVerticalPadding
  >
    {!vehicle ? EMPTY_CELL_CONTENT : (
      <PersonalCard
        title={vehicle.stateNumber}
        desc1={getFullVehicle(vehicle)}
        desc2={vehicle.color}
        src={Taxi}
      />
    )}
  </Panel>
));
