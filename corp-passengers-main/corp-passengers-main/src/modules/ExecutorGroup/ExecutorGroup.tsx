/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { Router as ExecutorGroupRouter } from 'modules/ExecutorGroup/ExecutorGroupRouter';

export const ExecutorGroup: FC = observer((): JSX.Element => {
  return (
    <ExecutorGroupRouter />
  );
});

export default ExecutorGroup;
