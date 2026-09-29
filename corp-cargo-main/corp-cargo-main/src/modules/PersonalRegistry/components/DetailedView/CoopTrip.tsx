import React, { FC } from 'react';
import { Descriptions } from 'antd';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { useTranslation } from 'i18n';
import uuid from 'utils/uuid';
import styles from 'shared/styles/reportsDetailedView.module.scss';
import { checkPersonalShownLabel } from 'utils/reportsUtils';
import { CoopTripField } from 'modules/TaxiRegistry/Components/DetailedView/CoopTripFields';
import { useCoopTripDescription } from '../../hooks/useCoopTripDescription';

export interface CoopTripProps { trip: TripResponse }

export const CoopTrip: FC<CoopTripProps> = ({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;

  const records = useCoopTripDescription(trip, labels).map(([label, description]) => {
    const shownLabel = checkPersonalShownLabel(trip, label, description);
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
