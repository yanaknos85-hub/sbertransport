
import React, { FC } from 'react';
import { ExecutorSettingsContext, Props } from './ExecutorSettings.context';

const ExecutorSettingsProvider: FC<{ value: Props }> = ({ value, children }) => {
  return (
    <ExecutorSettingsContext.Provider value={value}>
      {children}
    </ExecutorSettingsContext.Provider>
  );
};

export default ExecutorSettingsProvider;
