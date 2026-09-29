import { TripResponse } from 'stores/Registry/Registry.interface';
import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { Descriptions } from 'antd';
import { usePersonalSearch } from 'api/personal-search';
import { checkPersonalShownLabel } from 'utils/reportsUtils';
import { useTripDescription } from '../../hooks/useTripDescription';

export interface TripProps { trip: TripResponse }

export const Trip: FC<TripProps> = ({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;

  const { data: reportData } = usePersonalSearch(
    { pageSetting: { page: 0, size: 1 }, requestHumanId: trip.humanReadableId },
    trip.passenger?.organizationId
  );

  const [tripReport] = reportData.content;

  // @ts-ignore
  // eslint-disable-next-line @stylistic/max-len
  const records = useTripDescription(trip, tripReport, labels).map(([label, description]) => checkPersonalShownLabel(trip, label, description)
  );

  return (
    <Descriptions size="small" column={1}>
      {records}
    </Descriptions>
  );
};
