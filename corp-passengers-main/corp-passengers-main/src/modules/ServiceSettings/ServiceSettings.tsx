import React, { FC } from 'react';
import Panel from 'components/Panel';
import { useTranslation } from 'i18n';
import { Stepper } from 'components/Stepper';
import { useStep } from './hooks/useStep';
import { Steps, STEPS } from './constants.settings';
import SettingsPanelHeader from './components/PanelHeader';

import ServiceSettingsRouter from './ServiceSettingsRouter';

import styles from './ServiceSettings.module.scss';

const Settings: FC = () => {
  const {
    t: { settings },
  } = useTranslation();

  const [step, setStep] = useStep(Object.values(Steps));

  return (
    <Panel>
      <SettingsPanelHeader title={settings.title} subTitle={settings.subTitle} />
      <Stepper
        currentStep={step}
        steps={STEPS}
        setStep={setStep}
        className={styles.settingsStepper}
      />
      <ServiceSettingsRouter />
    </Panel>
  );
};

export default Settings;
