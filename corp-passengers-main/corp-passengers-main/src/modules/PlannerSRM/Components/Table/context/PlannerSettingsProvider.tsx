
import React, { FC } from 'react';
import { PlannerSettingsContext, Props } from './PlannerSettings.context';

const PlannerSettingsProvider: FC<{ value: Props }> = ({ value, children }) => {
  return (
    <PlannerSettingsContext.Provider value={value}>
      {children}
    </PlannerSettingsContext.Provider>
  );
};

export default PlannerSettingsProvider;
