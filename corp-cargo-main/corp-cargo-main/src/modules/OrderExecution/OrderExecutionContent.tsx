import React, { useEffect, useRef, useCallback } from 'react';
import { ToolbarProvider } from 'components/Toolbar';
import OrderTable from './components/OrderTable/Cargo';
import FilterSection from './components/FilterSection';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import SchedulerTable from './components/Scheduler/SchedulerTable';
import { observer } from 'mobx-react';
import { useOrganizationContext } from 'context/Organization.context';
import { UUID } from 'utils/io-ts';

const OrderExecutionContent = () => {
  const { cargoStore } = useAppStoreContext();
  const { organizationId, executorGroupId, isOrganization, emptyExecutorGroup } = useOrganizationContext();

  const prevActiveCategory = useRef(cargoStore.activeCategory);
  const prevOrgData = useRef({ organizationId, executorGroupId, isOrganization, emptyExecutorGroup });
  const isInitialMount = useRef(true);

  // Общая функция загрузки данных
  const loadData = useCallback(() => {
    if (cargoStore.activeCategory === 'template') {
      cargoStore.getCargoSchedulerList();
    } else {
      cargoStore.setCargoFilterQueryProps({
        transportType: cargoStore.activeCategory !== 'all'
          ? cargoStore.activeCategory.toUpperCase()
          : undefined,
      });
      cargoStore.getCargoOrderListDeferredPost(emptyExecutorGroup);
    }
  }, [cargoStore.activeCategory, emptyExecutorGroup]);

  // Обновление данных организации в сторе
  useEffect(() => {
    cargoStore.setOrganizationId(organizationId as UUID);
    cargoStore.setExecutorGroupId(executorGroupId);
    cargoStore.setIsOrganization(isOrganization);
  }, [organizationId, executorGroupId, isOrganization, cargoStore]);

  // Единый эффект для всех изменений
  // Срабатывает только когда активная категория или организация реально изменились
  useEffect(() => {
    const isFirstMount = isInitialMount.current;
    if (isFirstMount) {
      isInitialMount.current = false;
    }

    const categoryChanged = prevActiveCategory.current !== cargoStore.activeCategory;
    const orgDataChanged =
      prevOrgData.current.organizationId !== organizationId ||
      prevOrgData.current.executorGroupId !== executorGroupId ||
      prevOrgData.current.isOrganization !== isOrganization ||
      prevOrgData.current.emptyExecutorGroup !== emptyExecutorGroup;

    // Если ничего не изменилось - выходим
    if (!isFirstMount && !categoryChanged && !orgDataChanged) {
      return;
    }

    // Обновляем предыдущие значения
    if (categoryChanged) {
      prevActiveCategory.current = cargoStore.activeCategory;
    }
    if (orgDataChanged) {
      prevOrgData.current = { organizationId, executorGroupId, isOrganization, emptyExecutorGroup };
    }

    // Загружаем данные
    loadData();
  }, [
    cargoStore.activeCategory,
    organizationId,
    executorGroupId,
    isOrganization,
    emptyExecutorGroup,
    loadData
  ]);

  return (
    <ToolbarProvider>
      <FilterSection />
      {cargoStore.activeCategory === 'template' ? <SchedulerTable /> : <OrderTable />}
    </ToolbarProvider>
  );
};

export default observer(OrderExecutionContent);
