
import React, { FC } from 'react';
import { TariffSettingsContext, Props } from '../context/TariffSettings.context';

const TariffSettingsProvider: FC<{ value: Props }> = ({ value, children }) => {
  return (
    <TariffSettingsContext.Provider value={value}>
      {children}
    </TariffSettingsContext.Provider>
  );
};

export default TariffSettingsProvider;
