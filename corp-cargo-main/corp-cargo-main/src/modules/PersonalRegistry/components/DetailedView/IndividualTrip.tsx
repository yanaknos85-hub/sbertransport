import { TripResponse } from 'stores/Registry/Registry.interface';
import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { Descriptions } from 'antd';
import { usePersonalSearch } from 'api/personal-search';
import { checkPersonalShownLabel } from 'utils/reportsUtils';
import { useIndividualTripDescription } from '../../hooks/useIndividualTripDescription';

export interface IndividualTripProps { trip: TripResponse }

export const IndividualTrip: FC<IndividualTripProps> = ({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;

  const { data: reportData } = usePersonalSearch(
    { pageSetting: { page: 0, size: 1 }, requestHumanId: trip.humanReadableId },
    trip.passenger?.organizationId
  );

  const [tripReport] = reportData.content;

  const records = useIndividualTripDescription(trip, tripReport, labels).map(([label, description]) => checkPersonalShownLabel(trip, label, description)
  );

  return (
    <Descriptions size="small" column={1}>
      {records}
    </Descriptions>
  );
};
