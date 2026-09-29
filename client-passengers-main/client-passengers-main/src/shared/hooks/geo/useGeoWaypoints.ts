import { usePrevious } from 'ahooks';
import { useEffect, useState } from 'react';

import { WaypointModel } from 'shared/models/geo/Waypoint.model';

interface HookProps {
  initWaypoints?: WaypointModel[];
}

export interface GeoWaypoints {
  waypoints: WaypointModel[];
  prevWaypoints: WaypointModel[] | undefined;
  setWaypoints: (items: WaypointModel[]) => WaypointModel[];
  editWaypoint: (index: number, value: WaypointModel) => WaypointModel[];
  addWaypoint: () => WaypointModel[];
  removeWaypoint: (index: number) => WaypointModel[];
}

const useGeoWaypoints = (props?: HookProps): GeoWaypoints => {
  const [waypoints, _setWaypoints] = useState<WaypointModel[]>(props?.initWaypoints || []);
  const prevWaypoints = usePrevious(waypoints);

  function setWaypoints(items: WaypointModel[]): WaypointModel[] {
    _setWaypoints(items);
    return items;
  }

  function editWaypoint(index: number, value: WaypointModel): WaypointModel[] {
    const items = [...waypoints];
    items[index] = value;
    _setWaypoints(items);
    return items;
  }

  function addWaypoint(): WaypointModel[] {
    const newState = [...waypoints, new WaypointModel()];
    _setWaypoints(newState);
    return newState;
  }

  function removeWaypoint(index: number): WaypointModel[] {
    const array = [...waypoints.filter((x, idx) => idx !== index)];
    if (array.length === 2) {
      array[1].waitTimeMinutes = 0;
    }
    _setWaypoints(array);
    return array;
  }

  // unmount
  useEffect(
    () => (): void => {
      _setWaypoints([]);
    },
    []
  );

  return {
    waypoints,
    prevWaypoints,
    setWaypoints,
    editWaypoint,
    addWaypoint,
    removeWaypoint,
  };
};

export default useGeoWaypoints;
