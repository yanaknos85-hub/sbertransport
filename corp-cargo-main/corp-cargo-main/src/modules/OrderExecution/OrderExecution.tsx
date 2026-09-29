/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { devRouter as OrderExecutionRouter } from 'modules/OrderExecution/OrderExecutionRouter';

export const OrderExecution: FC = observer((): JSX.Element => {
  return (
    <OrderExecutionRouter />
  );
});

export default OrderExecution;
