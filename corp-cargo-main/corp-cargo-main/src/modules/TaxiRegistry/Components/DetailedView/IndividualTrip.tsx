import React, { FC } from 'react';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { Descriptions } from 'antd';
import { useTranslation } from 'i18n';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { checkTaxiShownLabel } from 'utils/reportsUtils';
import { useIndividualTripDescription } from '../../hooks/useIndividualTripDescription';

export interface IndividualTripProps { trip: TripResponse; factTrip: TaxiTripFactData }

export const IndividualTrip: FC<IndividualTripProps> = ({ trip, factTrip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.informationAttributesOfRegistries;

  const records = useIndividualTripDescription(trip, factTrip, labels).map(([label, description]) => checkTaxiShownLabel(trip, label, description)
  );

  return (
    <Descriptions size="small" column={1}>
      {records}
    </Descriptions>
  );
};
