import React, { FC } from 'react';
import { ReactComponent as StepperArrowIcon } from 'shared/icons/stepper-arrow.svg';
import { StepperWrapper, StepStyled } from './Stepper.styled';

interface StepperProps {
  currentStep: number;
  steps: string[];
  setStep?: (step: StepperProps['currentStep']) => void;
  className?: string;
}

export const Stepper: FC<StepperProps> = ({
  currentStep, steps, setStep, className,
}) => (
  <StepperWrapper className={className}>
    {steps.map((step, index) => (
      <StepStyled
        key={index}
        isActive={index <= currentStep}
        isCurrent={index === currentStep}
        onClick={() => setStep?.(index)}
        isClickable={!!setStep}
      >
        <span>{index + 1}</span>
        {step}
        {index < steps.length - 1 && <StepperArrowIcon />}
      </StepStyled>
    ))}
  </StepperWrapper>
);
