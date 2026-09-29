import React, { FC } from 'react';
import { CustomersContext, Props } from './Customers.context';

const CustomersProvider: FC<{ value: Props }> = ({ value, children }) => {
  return (
    <CustomersContext.Provider value={value}>
      {children}
    </CustomersContext.Provider>
  );
};

export default CustomersProvider;
