import React, { FC } from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useTranslation } from 'i18n';
import { Icon } from './Icon/Icon';
import { useForm } from 'antd/lib/form/Form';
import { OrderListMinimalType, OrdersListMinimalStoreType } from '../../types';
import { usePlanner } from '../../context/PlannerContext';
import { ReactComponent as AutoPlanningIcon } from 'modules/Planner/images/autoPlanning.svg';
import { useAutoPlanning } from 'api/planner';
import { FilterOrderInput } from '../Monitor/FilterOrderInput/FilterInput';
import { SortMenu } from 'modules/Planner/SortMenu/SortMenu';
import { useRole } from 'utils/useRole';
import { Roles } from 'constants/constants.app';
import * as S from './OrderControls.styles';

interface Props {
  routeId?: string;
  handleFilters: () => void;
  resetFilters: () => void;
  onSortChange?: (directionAsc: boolean, sortingProperty: string) => void;
  setPagePagination: (page: number) => void;
  ordersListStore: OrdersListMinimalStoreType;
  isAddToRoute?: boolean;
  handleRefetchAutoPlanning?: () => void;
}

export const OrderControls: FC<Props> = props => {
  const {
    handleFilters, onSortChange, setPagePagination, ordersListStore, isAddToRoute, handleRefetchAutoPlanning,
  } = props;
  const [form] = useForm();
  const { refetch } = useAutoPlanning({});

  const { plannerStore } = useAppStoreContext();
  const { t } = useTranslation();
  const roles = useRole();

  const isCurrentUserAdmin = roles.some(currentUserRole => currentUserRole === Roles.ADMIN_DATA_MASTER);

  const {
    checkedOrdersListStore, addOrderToRoute, clearCheckedListStore,
  } = plannerStore;
  const { setOrderFilters } = usePlanner();

  const clearOrdersList = () => {
    clearCheckedListStore();
  };

  const fillOrdersList = () => {
    ordersListStore?.content?.forEach((minimalOrder) => {
      addOrderToRoute(minimalOrder);
    });
  };

  const handleAutoPlanning = () => {
    refetch();
    handleRefetchAutoPlanning && handleRefetchAutoPlanning();
  };

  return (
    <S.ControlsStyled>
      <S.Wrapper>
        <FilterOrderInput getFilteredRoutes={setOrderFilters} filtersForm={form} handleFilters={handleFilters}/>
        <S.ButtonsBlock>
          {isCurrentUserAdmin && !isAddToRoute && (
            <Icon
              onClick={handleAutoPlanning}
              icon={<AutoPlanningIcon />}
              tooltip={t.Planner.autoPlanning}
            />
          )}
        </S.ButtonsBlock>
      </S.Wrapper>
      <S.Wrapper>
        <SortMenu onSortChange={onSortChange} setPagePagination={setPagePagination} />
        {!checkedOrdersListStore?.length && (
          <S.GreenTextButton onClick={fillOrdersList}>Выделить все</S.GreenTextButton>
        )}
        {!!checkedOrdersListStore?.length && (
          <S.GreenTextButton onClick={clearOrdersList}>Снять выделение</S.GreenTextButton>
        )}
      </S.Wrapper>
    </S.ControlsStyled>
  );
};
