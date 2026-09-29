import React, { FC, useState } from 'react';
import { FilterRequest } from '../types/types';
import { BusinessReportsContext, BusinessReportsContextProps } from './BusinessReports.context';

interface ProviderProps {
  children: React.ReactNode;
}

const BusinessReportsProvider: FC<ProviderProps> = ({ children }) => {
  const [initialFilters, setInitialFilters] = useState<FilterRequest | undefined>();

  const value: BusinessReportsContextProps = {
    initialFilters,
    setInitialFilters,
  };

  return (
    <BusinessReportsContext.Provider value={value}>
      {children}
    </BusinessReportsContext.Provider>
  );
};

export default BusinessReportsProvider;
