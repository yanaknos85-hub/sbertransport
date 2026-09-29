
import React, { FC } from 'react';
import { TripSettingsContext, Props } from '../context/TripSettings.context';

const TripSettingsProvider: FC<{ value: Props }> = ({ value, children }) => {
  return (
    <TripSettingsContext.Provider value={value}>
      {children}
    </TripSettingsContext.Provider>
  );
};

export default TripSettingsProvider;
