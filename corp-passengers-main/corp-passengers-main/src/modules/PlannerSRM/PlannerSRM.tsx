/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { Router as PlannerSRMRouter } from 'modules/PlannerSRM/PlannerSRMRouter';

export const PlannerSRM: FC = observer((): JSX.Element => {
  return (
    <PlannerSRMRouter />
  );
});

export default PlannerSRM;
