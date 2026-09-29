/* eslint-disable no-console */
import React, { MutableRefObject } from 'react';

export interface Props {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  footer: MutableRefObject<any>;
}

export const CustomersContext = React.createContext({} as Props);

export const useCustomersContext = () => {
  const context = React.useContext(CustomersContext);

  if (context === undefined) {
    const error = 'CustomersContext.Context must be used within a CustomersContext.Provider';

    throw new Error(error);
  }

  return context;
};
