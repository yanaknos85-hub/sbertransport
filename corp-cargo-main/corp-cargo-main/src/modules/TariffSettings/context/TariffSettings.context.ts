/* eslint-disable no-console */
import React, { MutableRefObject } from 'react';

export interface Props {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  footer: MutableRefObject<any>;
}

export const TariffSettingsContext = React.createContext({} as Props);

export const useTariffSettingsContext = () => {
  const context = React.useContext(TariffSettingsContext);

  if (context === undefined) {
    const error = 'TariffSettings.Context must be used within a TariffSetting.Provider';
    console.error(error);
    throw new Error(error);
  }

  return context;
};
