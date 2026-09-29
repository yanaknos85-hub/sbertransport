import { useTripRequest } from 'api/registry';

import React, { FC, Suspense } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { UUID } from 'utils/io-ts';

import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { Trip } from './Trip';

import styles from 'shared/styles/reportsDetailedView.module.scss';

const DetailedView: FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const match = useRouteMatch<any>();
  const { id } = match.params;

  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.CARSHARING, requestId: id as UUID });

  return (
    <div className={styles.tripDetailedView}>
      <div className={styles.mapWrapper}>
        <MapComponent
          markers={tripResponse.tripResponse?.expected?.waypoints as WaypointModel[]}
          polylines={tripResponse.tripResponse?.expected?.segments}
          className={styles.map}
          fitToShowAllGeometry
          dragging
          zoomControl
        />
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.tableWrapper}>
          <Trip trip={tripResponse.tripResponse} />
        </div>
      </Suspense>
    </div>
  );
};

export default withErrorBoundary(DetailedView);
