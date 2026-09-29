import React, { useState } from 'react';

import { Steps as AntSteps } from 'antd';
import { useTranslation } from 'i18n';

import { Button } from 'shared/components/Button/Button';
import { StepItems } from './types';

import styles from './Steps.module.scss';

const { Step } = AntSteps;

export const Steps: React.FC<{
  headerTitle?: string;
  headerSubtitle?: string;
  items: StepItems;
  currentStep?: number;
  setCurrentStep?: React.Dispatch<React.SetStateAction<number>>;
  hasOwnControlButtons?: boolean;
}> = ({
  headerTitle, headerSubtitle, items, hasOwnControlButtons, currentStep, setCurrentStep,
}) => {
  const [current, setCurrent] = useState(0);
  const { t } = useTranslation();

  const nextStep = (): void => {
    if (currentStep !== undefined && setCurrentStep && currentStep < items.length - 1) {
      setCurrentStep(currentStep + 1);
    } else if (current < items.length - 1) {
      setCurrent(current + 1);
    }
  };

  const prevStep = (): void => {
    if (currentStep !== undefined && setCurrentStep && currentStep > 0) {
      setCurrentStep(currentStep - 1);
    } else if (current > 0) {
      setCurrent(current - 1);
    }
  };

  const onFinish = (): void => {
    // TODO some action from props
  };

  return (
    <>
      <div className={styles.stepsContainer}>
        {headerTitle && <h2 className={styles.title}>{headerTitle}</h2>}
        {headerSubtitle && <p className={styles.subtitle}>{headerSubtitle}</p>}
        <hr className={styles.headerDivider} />

        <AntSteps
          type="navigation"
          current={currentStep ?? current}
          className={styles.settingsSteps}
        >
          {items.map(item => (
            <Step
              key={item.title}
              title={item.title}
              // icon={<StepIcon step={index + 1} />}
            />
          ))}
        </AntSteps>
        <div className={styles.stepContent}>{items[currentStep !== undefined ? currentStep : current].content}</div>
      </div>

      {hasOwnControlButtons && (
        <div className={styles.buttonBlock}>
          <Button
            className={styles.buttonBack}
            htmlType="button"
            size="middle"
            onClick={prevStep}
            disabled={current === 0}
          >
            {t.global.stepBack}
          </Button>

          {current === items.length - 1 ? (
            <Button
              htmlType="button"
              size="middle"
              type="primary"
              onClick={onFinish}
            >
              {t.global.success}
            </Button>
          ) : (
            <Button
              htmlType="button"
              size="middle"
              type="primary"
              onClick={nextStep}
            >
              {t.global.stepForward}
            </Button>
          )}
        </div>
      )}
    </>
  );
};
