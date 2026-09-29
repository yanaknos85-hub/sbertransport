import React, { FC } from 'react';
import { Steps } from 'antd';

import { LIMIT_REQUEST_STATUS, LimitRequestStatusesTitlesEnum } from 'stores/Limits/Limit.interface';
import styles from './styles.module.scss';

const { Step } = Steps;

interface StepperProps {
  currentStatus: LIMIT_REQUEST_STATUS;
  subTitle?: string;
}

const stepperCancelStatuses = ['CANCELLED', 'DECLINED'];
const stepperFinishedStatuses = ['DONE_FULLY', 'DONE_PARTLY', 'APPROVED', 'DONE'];

const Stepper: FC<StepperProps> = ({ currentStatus, subTitle }) => {
  const isCanceledStatus = stepperCancelStatuses.some(el => el === currentStatus);
  const isSuccessStatus = stepperFinishedStatuses.some(el => el === currentStatus);

  return (
    <Steps
      className={styles.stepper}
      size="small"
      current={0}
      direction="vertical"
    >
      <Step
        title={LimitRequestStatusesTitlesEnum.INIT}
        status={isCanceledStatus ? 'error' : 'wait'}
        subTitle={subTitle}
      />
      <Step
        title={LimitRequestStatusesTitlesEnum[currentStatus]}
        status={isCanceledStatus ? 'error' : isSuccessStatus ? 'finish' : 'wait'}
      />
    </Steps>
  );
};

export default Stepper;
