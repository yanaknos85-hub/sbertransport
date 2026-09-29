import React from 'react';
import { FilterRequest } from '../types/types';

export interface BusinessReportsContextProps {
  initialFilters?: FilterRequest;
  setInitialFilters?: React.Dispatch<React.SetStateAction<FilterRequest | undefined>>;
}

export const BusinessReportsContext = React.createContext({} as BusinessReportsContextProps);

export const useBusinessReportsContext = () => {
  const context = React.useContext(BusinessReportsContext);

  if (context === undefined) {
    const error = 'BusinessReportsContext must be used within a BusinessReportsProvider';

    throw new Error(error);
  }

  return context;
};
