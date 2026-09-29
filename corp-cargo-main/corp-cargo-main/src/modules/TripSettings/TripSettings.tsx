import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { useTranslation } from 'i18n';
import Panel from 'components/Panel';
import { Stepper } from 'components/Stepper';
import TripRulesPanelHeader from './components/PanelHeader';

import { useStep } from './hooks/useStep';

import { Steps } from './constants/tripSettings';

import TripSettingsRouter from './TripSettingsRouter';

import TripSettingsProvider from './context/TripSettingsProvider';

import styles from './TripSettings.module.scss';

export const TripSettings: FC = observer((): JSX.Element => {
  const { t } = useTranslation();

  const [step, setStep] = useStep(Object.values(Steps));
  const STEPS = [
    t.SettingsTripRules.serviceTypes.title,
    t.SettingsTripRules.tripPurposes.title,
    t.SettingsTripRules.approvals.title,
    t.SettingsTripRules.sharedRides.title,
  ];

  const nextStep = () => {
    if (step < Object.values(Steps).length - 1) {
      setStep(step + 1);
    }
  };

  const prevStep = () => {
    if (step > 0) {
      setStep(step - 1);
    }
  };

  return (
    <div className={styles.panelWrapper}>
      <Panel className={styles.panel}>
        <TripRulesPanelHeader title={t.SettingsTripRules.headerTitle} subTitle={t.SettingsTripRules.headerSubtitle} />
        <Stepper
          currentStep={step}
          steps={STEPS}
          setStep={setStep}
        />
        <TripSettingsProvider
          value={{
            stepper: {
              steps: STEPS,
              step,
              nextStep,
              prevStep,
            },
          }}
        >
          <TripSettingsRouter />
        </TripSettingsProvider>
      </Panel>
    </div>
  );
});

export default TripSettings;
