import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { Section, Item } from '../../Item';

import styles from './styles.module.scss';

interface QrsSectionProps {
  qrs?: string[];
}

export const QrsSection: FC<QrsSectionProps> = ({ qrs }) => {
  const { t } = useTranslation();

  if (!qrs || qrs.length === 0) {
    return null;
  }

  return (
    <Section title={t.orderExecution.cargo.qrsTitle}>
      <div className={styles.qrsList}>
        {qrs.map((code, index) => (
          <Item key={index}>{code}</Item>
        ))}
      </div>
    </Section>
  );
};

export default QrsSection;
