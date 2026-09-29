import { Descriptions } from 'antd';
import React, { FC, Suspense } from 'react';

import { useTripRequest } from 'api/registry';
import { UUID } from 'utils/io-ts';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useRouteMatch } from 'react-router-dom';
import { useDescriptionItemRecords } from './UseDescriptionItemRecords';

import styles from 'shared/styles/reportsDetailedView.module.scss';

export const DetailedView: FC = () => {
  const match = useRouteMatch<any>();
  const { id: requestId } = match.params;
  const { data: tripResponse, isLoading } = useTripRequest({
    transportType: 'PUBLIC',
    requestId: requestId as UUID,
  });
  const records = useDescriptionItemRecords(tripResponse.tripResponse);

  if (isLoading) {
    return <SpinWrapped />;
  }

  return (
    <div className={styles.tripDetailedView}>
      <div className={styles.mapWrapper}>
        <MapComponent
          className={styles.map}
          markers={tripResponse.tripResponse?.expected?.waypoints as WaypointModel[]}
          polylines={tripResponse.tripResponse?.expected?.segments}
          dragging
        />
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.tableWrapper}>
          <Descriptions size="small" column={1}>
            {records.map(([label, description]) => (
              <Descriptions.Item
                key={label}
                label={label}
                className={styles.label}
              >
                <span className={styles.coopTripItem}>{description}</span>
              </Descriptions.Item>
            ))}
          </Descriptions>
        </div>
      </Suspense>
    </div>
  );
};
