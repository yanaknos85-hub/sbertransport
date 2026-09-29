import { useTripRequest } from 'api/registry';

import React, { FC, Suspense } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { UUID } from 'utils/io-ts';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { IndividualTrip } from './IndividualTrip';
import { CoopTrip } from './CoopTrip';

import styles from 'shared/styles/reportsDetailedView.module.scss';

const DetailedView: FC = () => {
  const match = useRouteMatch<any>();
  const { id } = match.params;

  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.PERSONAL, requestId: id as UUID });

  return (
    <div className={styles.tripDetailedView}>
      <div className={styles.mapWrapper}>
        <MapComponent
          markers={tripResponse.tripResponse?.expected?.waypoints as WaypointModel[]}
          polylines={tripResponse.tripResponse?.expected?.segments}
          className={styles.map}
          dragging
          zoomControl
          fitToShowAllGeometry
        />
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.tableWrapper}>
          {tripResponse.tripResponse.coopTrip ? (
            <CoopTrip trip={tripResponse.tripResponse} />
          ) : (
            <IndividualTrip trip={tripResponse.tripResponse} />
          )}
        </div>
      </Suspense>
    </div>
  );
};

export default withErrorBoundary(DetailedView);
