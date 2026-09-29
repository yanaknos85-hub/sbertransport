import React, { FC } from 'react';
import { Modal } from 'antd';
import cn from 'classnames';

import { Waypoint } from 'api/trips-cargo/trips-cargo.types';
import Flex from 'components/Flex/Flex';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import { getAddress } from 'utils/getAddress';
import { useTripsModal } from '../../context/TripsModal';

import styles from './adresses.module.scss';

interface AddressProps {
  waypoints: Waypoint[];
}

export const Address: FC<AddressProps> = ({ waypoints }) => {
  const { openAddresses } = useTripsModal();

  if (!waypoints.length) {
    return <span>{EMPTY_CELL_CONTENT}</span>;
  }

  const handleOpenAddresses = () => {
    openAddresses(waypoints);
  };

  return (
    <Flex>
      <div
        className={styles.waypoint}
        title={waypoints[0].fullAddress ?? getAddress(waypoints[0])}
        onClick={handleOpenAddresses}
      >
        {waypoints[0].fullAddress ?? getAddress(waypoints[0])}
      </div>

      {waypoints.length > 1 && (
        <div
          className={cn(styles.waypoint, styles.waypointLength)}
          onClick={handleOpenAddresses}
        >
          {`+${waypoints.length - 1}`}
        </div>
      )}
    </Flex>
  );
};

export const AddressModal: FC = () => {
  const {
    isOpened, modalState, closeModal,
  } = useTripsModal();

  return (
    <Modal
      title="Промежуточные адреса"
      className={styles.modal}
      visible={isOpened('addresses')}
      onCancel={closeModal}
      destroyOnClose
      footer={[]}
      width={720}
    >
      {modalState.waypoints?.map(waypoint => (
        <div key={waypoint.index}>
          {waypoint.fullAddress ?? getAddress(waypoint)}
        </div>
      ))}
    </Modal>
  );
};

