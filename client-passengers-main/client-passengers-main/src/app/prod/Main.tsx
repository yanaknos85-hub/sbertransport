import React from 'react';
import type { FC } from 'react';
import AppRouter from 'AppRouter';
import { SpinWrapped } from 'shared/components';
import { hooks } from 'stores';

export const Main: FC = () => {
  // Set Default Store Values!
  const { isSelfEmployeeLoaded } = hooks.useInitStore();

  if (!isSelfEmployeeLoaded) {
    return <SpinWrapped />;
  }

  return <AppRouter />;
};

export default Main;
