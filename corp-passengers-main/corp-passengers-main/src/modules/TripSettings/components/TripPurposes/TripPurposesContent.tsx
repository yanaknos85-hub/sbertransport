import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { Button } from 'shared/components/Button/Button';
import { useTripSettingsContext } from '../../context/TripSettings.context';

import TripPurposesJournalNew from 'modules/TripPurposes/components/TripPurposesJournalNew';

import styles from '../../TripSettings.module.scss';

const TripPurposeContent: FC = () => {
  const { t } = useTranslation();
  const { stepper: { nextStep, prevStep } } = useTripSettingsContext();

  return (
    <>
      <TripPurposesJournalNew />

      <div className={styles.controlButtons}>
        <Button
          htmlType="button"
          size="middle"
          onClick={prevStep}
        >
          {t.global.stepBack}
        </Button>
        <Button
          htmlType="button"
          size="middle"
          type="primary"
          onClick={nextStep}
        >
          {t.global.stepForward}
        </Button>
      </div>
    </>
  );
};

export default TripPurposeContent;
