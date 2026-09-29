import React, { FC, Suspense } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { UUID } from 'utils/io-ts';
import { useTripRequest, useTripRequestWithFactData } from 'api/registry';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { MapComponent } from 'shared/components/Map/MapComponent';

import { IndividualTrip } from './IndividualTrip';
import { CoopTrip } from './CoopTrip';

import styles from 'shared/styles/reportsDetailedView.module.scss';

const DetailedView: FC = () => {
  const match = useRouteMatch<{ id: string }>();
  const { id } = match.params;

  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.TAXI, requestId: id as UUID });
  const { data: factTrip } = useTripRequestWithFactData({ transportType: TRANSPORT_TYPE.TAXI, requestId: id as UUID });

  return (
    <div className={styles.tripDetailedView}>
      <div className={styles.mapWrapper}>
        <MapComponent
          markers={tripResponse.tripResponse.expected?.waypoints as WaypointModel[]}
          polylines={tripResponse.tripResponse.expected.segments}
          className={styles.map}
          dragging
          zoomControl
        />
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.tableWrapper}>
          {tripResponse.tripResponse.coopTrip ? (
            <CoopTrip trip={tripResponse.tripResponse} factTrip={factTrip} />
          ) : (
            <IndividualTrip trip={tripResponse.tripResponse} factTrip={factTrip} />
          )}
        </div>
      </Suspense>
    </div>
  );
};
export default withErrorBoundary(DetailedView);
