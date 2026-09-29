import Panel from 'components/Panel';
import React, { FC, useRef } from 'react';
import { Steps, STEPS } from './constants';
import { useStep } from '../../shared/hooks/useStep';
import { StyledFooter, StyledStepper } from './styled/styled.tariffs';

import TariffSettingsProvider from './context/TariffSettingsProvider';

import TariffsRouter from './TariffSettingsRouter';

const Tariffs: FC = () => {
  const [step, setStep] = useStep(Object.values(Steps));

  const footerRef = useRef<HTMLDivElement>(null);

  return (
    <>
      <Panel>
        <StyledStepper
          currentStep={step}
          steps={STEPS}
          setStep={setStep}
        />
        <TariffSettingsProvider value={{ footer: footerRef }}>
          <TariffsRouter />
        </TariffSettingsProvider>
      </Panel>
      <StyledFooter ref={footerRef} />
    </>
  );
};

export default Tariffs;
