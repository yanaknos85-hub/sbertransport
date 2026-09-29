/* eslint-disable no-console */
import React, { MutableRefObject } from 'react';

export interface Props {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  footer: MutableRefObject<any>;
}

export const PlannerSettingsContext = React.createContext({} as Props);

export const useExecutorSettingsContext = () => {
  const context = React.useContext(PlannerSettingsContext);

  if (context === undefined) {
    const error = 'ExecutorSettings.Context must be used within a ExecutorSettings.Provider';
    console.error(error);
    throw new Error(error);
  }

  return context;
};
