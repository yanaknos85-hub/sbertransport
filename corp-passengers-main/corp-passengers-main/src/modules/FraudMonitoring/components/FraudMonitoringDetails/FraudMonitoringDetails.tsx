import React from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useFraudMonitoringDetails } from 'api/fraud-monitoring';

import { TitleSection } from './components/TitleSection/TitleSection';
import { FraudSection } from './components/FraudSection/FraudSection';
import { InformationSection } from './components/InformationSection/InformationSection';

import styles from './styles.module.scss';

export const FraudMonitoringDetails = () => {
  const match = useRouteMatch<{ id: string }>();
  const { id: fraudOrderId } = match.params;
  const details = useFraudMonitoringDetails(fraudOrderId).data;

  return (
    <div className={styles.container}>
      <TitleSection>{details.humanReadableId}</TitleSection>
      <FraudSection fraudItems={details.fraudMarkers} />
      <InformationSection fraudDetails={details} />
    </div>
  );
};
