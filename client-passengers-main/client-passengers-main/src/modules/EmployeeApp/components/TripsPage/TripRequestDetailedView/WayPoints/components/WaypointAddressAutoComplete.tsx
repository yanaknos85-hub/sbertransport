import React from 'react';

import AddressAutoComplete from 'shared/components/AddressAutoComplete';
import { useGeoAutoComplete } from 'shared/hooks/geo';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import styles from '../styles.module.scss';

const WaypointAddressAutoComplete = ({
  editWaypoint,
}: {
  editWaypoint: (value: WaypointModel) => WaypointModel[];
}): JSX.Element => {
  const { autoCompleteList, editSingleAddress } = useGeoAutoComplete();

  const selectHandler = (waypoint: WaypointModel | undefined): void => {
    if (waypoint) {
      editWaypoint(waypoint);
    }
  };

  return (
    <div className={styles.addressAutoComplete}>
      <AddressAutoComplete
        list={autoCompleteList}
        onSearch={editSingleAddress}
        onSelect={selectHandler}
      />
    </div>
  );
};

export default WaypointAddressAutoComplete;
