import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { useProfile } from 'api/profile';
import { ToolbarProvider } from 'components/Toolbar';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import OrderTable from './components/OrderTable/Passengers';
import { useTable } from './components/OrderTable/Passengers/useTable';
import FilterSection from './components/FilterSection';

const OrderExecutionContent: FC = () => {
  const { passengerStore } = useAppStoreContext();
  const { userId } = useProfile().data;
  const { columnsOrderExecution } = useTable(userId, passengerStore.personQueryFilters);

  return (
    <ToolbarProvider>
      <FilterSection columns={columnsOrderExecution} />
      <OrderTable columns={columnsOrderExecution} />
    </ToolbarProvider>
  );
};

export default observer(OrderExecutionContent);
