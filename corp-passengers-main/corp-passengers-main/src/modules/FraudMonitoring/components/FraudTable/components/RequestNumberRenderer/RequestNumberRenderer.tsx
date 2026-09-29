import React from 'react';
import { Link } from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { RequestNumberRendererProps } from './types';

import styles from './styles.module.scss';

export const RequestNumberRenderer = ({ children, id }: RequestNumberRendererProps) => {
  return (
    <Link className={styles.text} to={`${routes.FRAUD_MONITORING}/${id}`}>
      {children}
    </Link>
  );
};
