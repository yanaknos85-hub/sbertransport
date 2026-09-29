
import { Row } from 'antd';
import { FormListFieldData } from 'antd/lib/form/FormList';
import React, { Dispatch, SetStateAction } from 'react';

import { CurrentCoordinates } from 'shared/hooks/geo/useCurrentCoordinates';
import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';

import { WaypointAutoComplete } from './WaypointAutoComplete';
import { WaypointButtonCurrentLocation } from './WaypointButtonCurrentLocation';
import { WaypointButtonRemove } from './WaypointButtonRemove';
import { WaypointWaitingTime } from './WaypointWaitingTime';

export const WaypointRow = ({
  fields,
  field,
  geoWaypoints,
  index,
  setActiveWaypointIndex,
  currentCoordsState,
}: {
  fields: FormListFieldData[];
  field: FormListFieldData;
  index: number;
  geoWaypoints: GeoWaypoints;
  currentCoordsState: CurrentCoordinates;
  setActiveWaypointIndex: Dispatch<SetStateAction<number>>;
}): JSX.Element => {
  const isFirstWaypoint = index === 0;
  const isTransitWaypoint = fields.length > 2 && index !== 0 && index !== fields.length - 1;
  const displayRemoveButton = fields.length > 2 && index !== 0;

  return (
    <Row>
      <WaypointAutoComplete
        field={field}
        geoWaypoints={geoWaypoints}
        index={index}
        setActiveWaypointIndex={setActiveWaypointIndex}
      />
      {isTransitWaypoint && (
      <WaypointWaitingTime
        field={field}
        index={index}
        geoWaypoints={geoWaypoints}
      />
      )}
      {isFirstWaypoint && (
        <WaypointButtonCurrentLocation currentCoordsState={currentCoordsState} geoWaypoints={geoWaypoints} />
      )}
      {displayRemoveButton && <WaypointButtonRemove index={index} geoWaypoints={geoWaypoints} />}
    </Row>
  );
};
