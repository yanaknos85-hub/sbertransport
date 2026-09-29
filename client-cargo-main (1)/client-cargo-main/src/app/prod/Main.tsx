import React, { FC } from 'react';
import AppRouter from 'AppRouter';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { useInitStore } from 'stores';

const Main: FC = observer(() => {
  // Set Default Store Values!
  const { isSelfEmployeeLoaded } = useInitStore();

  if (!isSelfEmployeeLoaded) {
    return <SpinWrapped />;
  }

  return <AppRouter />;
});

export default Main;
