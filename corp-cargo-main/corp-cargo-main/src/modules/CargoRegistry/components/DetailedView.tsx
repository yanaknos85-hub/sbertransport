import React, { FC, Suspense, useMemo } from 'react';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Descriptions } from 'antd';
import { useTranslation } from 'i18n';
import { useCargoTripInfo, useSearchCargoRegistry } from 'api/cargo-registry-search';
import { useProfile } from 'api/profile';
import { useTransformedData } from '../hooks/useTransformedData';
import { VisibleFields } from '../constants';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';

import styles from 'shared/styles/reportsDetailedView.module.scss';

interface DetailedViewProps {
  id: string;
}

export const DetailedView: FC<DetailedViewProps> = ({ id }) => {
  const { organizationId } = useProfile().data;
  const { data: mainContent } = useCargoTripInfo(id);
  const {
    data: {
      content: [auxContent],
    },
  } = useSearchCargoRegistry({ requestHumanId: mainContent.humanReadableId }, {}, organizationId);

  const transformedData = useTransformedData([{ ...auxContent, ...mainContent }]);
  const { t } = useTranslation();

  const records = useMemo(() => {
    const labels = t.Forms.registryCargoSettings;
    const transformed = transformedData[0] || {};
    return (Object.keys(transformed) as VisibleFields[]).map(key => ({
      key,
      label: labels[key],
      value: transformed[key],
    }));
  }, [transformedData, t]);

  return mainContent ? (
    <div className={styles.tripDetailedView}>
      <div className={styles.mapWrapper}>
        <MapComponent
          className={styles.map}
          markers={mainContent.expected?.waypoints as unknown as WaypointModel[]}
          polylines={mainContent.expected?.segments}
          dragging
        />
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.tableWrapper}>
          <Descriptions size="small" column={1}>
            {records.map(({
              key, label, value,
            }) => (
              <Descriptions.Item
                key={key}
                label={label}
                className={styles.label}
              >
                <span className={styles.coopTripItem}>{value}</span>
              </Descriptions.Item>
            ))}
          </Descriptions>
        </div>
      </Suspense>
    </div>
  ) : null;
};
