import React, { FC } from 'react';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { useTranslation } from 'i18n';
import { Descriptions } from 'antd';
import uuid from 'utils/uuid';
import styles from 'shared/styles/reportsDetailedView.module.scss';
import { checkTaxiShownLabel } from 'utils/reportsUtils';
import { useCoopTripDescription } from '../../hooks/useCoopTripDescription';
import { CoopTripField } from './CoopTripFields';

export interface CoopTripProps { trip: TripResponse; factTrip: TaxiTripFactData }

export const CoopTrip: FC<CoopTripProps> = ({ trip, factTrip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.informationAttributesOfRegistries;

  const records = useCoopTripDescription(trip, factTrip, labels).map(([label, description]) => {
    const shownLabel = checkTaxiShownLabel(trip, label, description);
    return shownLabel === undefined ? (
      <Descriptions.Item
        key={uuid()}
        label={label}
        className={styles.label}
      >
        <CoopTripField description={description} />
      </Descriptions.Item>
    ) : (
      shownLabel
    );
  });

  return (
    <Descriptions size="small" column={1}>
      {records}
    </Descriptions>
  );
};
