import React from 'react';
import withErrorBoundary from 'components/withErrorBoundary';
import { useModalState } from 'hooks/useModal';
import DriverTitle from '../components/DriverTitle/DriverTitle';
import { TripStatuses } from 'constants/trips.constants';
import Panel from 'components/Panel/Panel';
import EditButton from 'components/EditButton/EditButton';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import PersonalCard from '../components/PersonalCard/PersonalCard';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { getFullName } from 'utils/getFullName';
import Rating from '../components/Rating/Rating';
import { SetDriverModal } from '../components/SetDriverModal/SetDriverModal';
import { useTripInfo } from '../context/TripInfo.context';

/** Водитель */
export const DriverCard = withErrorBoundary(() => {
  const { trip } = useTripInfo();

  const [isDriverModalVisible, { show: showDriverModal, hide: hideDriverModal }] = useModalState();

  const driver = trip.driver ?? trip.planned?.driver;

  return (
    <Panel
      fullHeight
      title={<DriverTitle isPlanned={!!trip.planned} />}
      smallVerticalPadding
      actions={TripStatuses[trip.status].isActive && <EditButton onClick={showDriverModal} />}
    >
      {!driver ? EMPTY_CELL_CONTENT : (
        <PersonalCard
          title={getFullName(driver)}
          desc1={`Тел ${driver.contactPhone ? formatPhoneNumber(driver.contactPhone) : EMPTY_CELL_CONTENT}`}
          desc2={!!driver.rating && <Rating rating={(driver.rating / 100).toFixed(2)} />}
        />
      )}

      <SetDriverModal
        trip={trip}
        visible={isDriverModalVisible}
        closeModal={hideDriverModal}
      />
    </Panel>
  );
});
