import React from 'react';
import { useHistory } from 'react-router-dom';
import { useTranslation } from 'i18n';

import { ReactComponent as ArrowLeft } from 'shared/icons/arrow-left.svg';

import styles from './styles.module.scss';

export const TitleSection = ({ children }: { children: string }) => {
  const {
    t: {
      fraudMonitoring: { details },
    },
  } = useTranslation();

  const history = useHistory();

  const handleBack = () => {
    history.goBack();
  };

  return (
    <div className={styles.container}>
      <ArrowLeft
        className={styles.backIcon}
        onClick={handleBack}
        data-testid="arrow-left"
      />
      <p className={styles.order}>
        {details.order}
        {' '}
        {children}
      </p>
    </div>
  );
};
