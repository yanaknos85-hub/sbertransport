import React from 'react';
import { DragDropContext, Draggable, Droppable } from 'react-beautiful-dnd';

import { DragIcon } from 'shared/components/SvgIcons';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { WaypointField } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import WaypointItem from './WaypointItem';

import styles from '../styles.module.scss';

const reorderWaypoints = (list: WaypointModel[], startIndex: number, endIndex: number): WaypointModel[] => {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);

  return result;
};

const DraggableWaypoints = ({
  tripRequestRoute,
  isCheckInState,
  waypointFields,
}: {
  tripRequestRoute: TripRequestRoute;
  isCheckInState: boolean;
  waypointFields: WaypointField[];
}): JSX.Element => {
  const { actualRoute, geoWaypoints } = tripRequestRoute;
  const { setWaypoints } = geoWaypoints;
  const { waypoints } = actualRoute;

  const onDragEnd = (result: any): void => {
    // dropped outside the list
    if (!result.destination) {
      return;
    }
    const items = reorderWaypoints(waypoints, result.source.index, result.destination.index);

    setWaypoints(items);
  };

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <Droppable droppableId="WayPointsDroppable">
        {(droppableProps): JSX.Element => (
          <div {...droppableProps.droppableProps} ref={droppableProps.innerRef}>
            {waypointFields.map((waypointField, waypointIndex) => (
              <Draggable
                key={waypointField.fieldName}
                draggableId={waypointField.fieldName}
                index={waypointIndex}
              >
                {(draggableProps): JSX.Element => (
                  <div
                    ref={draggableProps.innerRef}
                    {...draggableProps.draggableProps}
                    className={styles.dragItem}
                  >
                    <div {...draggableProps.dragHandleProps} className={styles.dragHandle}>
                      <DragIcon />
                    </div>
                    <WaypointItem
                      tripRequestRoute={tripRequestRoute}
                      waypointField={waypointField}
                      isCheckInState={isCheckInState}
                      isPersonalTransport
                      waypointIndex={waypointIndex}
                    />
                  </div>
                )}
              </Draggable>
            ))}
            {droppableProps.placeholder}
          </div>
        )}
      </Droppable>
    </DragDropContext>
  );
};

export default DraggableWaypoints;
