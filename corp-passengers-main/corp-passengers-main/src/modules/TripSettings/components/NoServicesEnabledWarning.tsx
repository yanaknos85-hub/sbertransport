import React from 'react';

import { useTranslation } from 'i18n';
import styles from '../TripSettings.module.scss';

export const NoServicesEnabledWarning: React.FC = () => {
  const { t } = useTranslation();
  return <div className={styles.noServicesEnabledWarning}>{t.SettingsTripRules.noServicesEnabledWarning}</div>;
};
