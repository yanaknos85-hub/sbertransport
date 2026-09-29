/* eslint-disable no-console */
import React from 'react';

interface Stepper {
  steps: string[];
  step: number;
  nextStep: () => void;
  prevStep: () => void;
}

export interface Props {
  stepper: Stepper;
}

export const TripSettingsContext = React.createContext({} as Props);

export const useTripSettingsContext = () => {
  const context = React.useContext(TripSettingsContext);

  if (context === undefined) {
    const error = 'TripSettings.Context must be used within a TripSettings.Provider';
    console.error(error);
    throw new Error(error);
  }

  return context;
};
