import React, { FC } from 'react';

import styles from '../Steps.module.scss';

export const StepIcon: FC<{ step: number }> = ({ step }) => <div className={styles.stepIcon}>{step}</div>;
