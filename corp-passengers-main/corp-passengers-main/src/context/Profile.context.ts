import React from 'react';
import { Employee } from '@sber-sbertransport/mf-core';

export const ProfileContext = React.createContext<Employee>({} as Employee);

export const useContextProfile = () => {
  const context = React.useContext(ProfileContext);

  if (context === undefined) {
    throw new Error('ProfileContext must be used within a ProfileContext.Provider');
  }

  return context;
};
