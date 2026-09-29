import React, { FC, Suspense, useMemo } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Descriptions } from 'antd';
import { useTranslation } from 'i18n';
import {
  useGetSearchRouteRegistry,
  useSearchCargoRegistry
} from 'api/cargo-registry-route-search';
import { useProfile } from 'api/profile';
import { useTransformedData } from '../hooks/useTransformedData';
import { VisibleFields } from '../constants';

import styles from 'shared/styles/reportsDetailedView.module.scss';

interface DetailedViewProps {
  id: string;
}

export const DetailedView: FC<DetailedViewProps> = ({ id }) => {
  const { organizationId } = useProfile().data;
  const { data: mainContent } = useGetSearchRouteRegistry(id);

  const { data: auxData } = useSearchCargoRegistry(
    { humanReadableId: mainContent?.humanReadableId, organizationId },
    { enabled: !!mainContent }
  );
  const auxContent = auxData?.content?.[0];

  const transformedData = useTransformedData(
    mainContent ? [{ ...auxContent, ...mainContent }] : []
  );
  const { t } = useTranslation();

  const records = useMemo(() => {
    if (!transformedData[0]) return [];
    const labels = t.Forms.registryCargoRoutesSettings;
    const transformed = transformedData[0] || {};
    return (Object.keys(transformed) as VisibleFields[]).map(key => ({
      key,
      label: labels[key],
      value: transformed[key],
    }));
  }, [transformedData, t]);

  if (!mainContent) return null;

  return (
    <div className={styles.tripDetailedView}>
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
  );
};
