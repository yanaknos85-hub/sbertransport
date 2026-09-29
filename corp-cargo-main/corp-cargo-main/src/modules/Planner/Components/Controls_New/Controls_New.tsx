import React, { FC } from 'react';

import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { usePlanner } from '../../context/PlannerContext';
import { RouteType } from '../../types';
import { Icon } from '../Icon/Icon';
import { ReactComponent as UpdateDataIcon } from '../../images/updateData.svg';
import { SortMenu } from 'modules/Planner/SortMenu/SortMenu';
import { FilterInput } from '../Monitor/FilterInput/FilterInput';

import * as S from './Controls_New.styles';

interface Props {
  routeId?: string;
  handleFilters: () => void;
  resetFilters: () => void;
  onSortChange?: (directionAsc: boolean, sortingProperty: string) => void;
  setPagePagination: (page: number) => void;
  routesListStore?: RouteType[];
  isAddToRoute?: boolean;
  handleRefetchAutoPlanning?: () => void;
  handleUpdateData?: () => void;
}

export const Controls_New: FC<Props> = props => {
  const {
    handleFilters,
    onSortChange,
    setPagePagination,
    routesListStore,
    handleUpdateData,
  } = props;

  const { t } = useTranslation();
  const [form] = useForm();

  const {
    checkedListRoutes, setCheckedListRoutes, setRouteFilters,
  } = usePlanner();

  const clearRoutesList = () => {
    setCheckedListRoutes([]);
  };

  const fillRoutesList = () => {
    setCheckedListRoutes(routesListStore?.map((item: RouteType) => item) as RouteType[]);
  };

  const isCheckAll = !!checkedListRoutes?.length

  return (
    <S.ControlsStyled>
      <S.Wrapper>
        <S.WrapperFilter>
          <FilterInput
            getFilteredRoutes={setRouteFilters}
            filtersForm={form}
            handleFilters={handleFilters}
          />
        </S.WrapperFilter>
        <Icon
          styles={{ width: '44px', height: '40px', cursor: 'pointer' }}
          onClick={handleUpdateData}
          icon={<UpdateDataIcon />}
          tooltip={t.Planner.updateData}
        />
      </S.Wrapper>
      <S.Wrapper>
        <SortMenu onSortChange={onSortChange} setPagePagination={setPagePagination}/>
        {isCheckAll ?
          <S.CheckRoutes onClick={clearRoutesList}>Снять выделение</S.CheckRoutes> :
          <S.CheckRoutes onClick={fillRoutesList}>Выделить все</S.CheckRoutes>
        }
      </S.Wrapper>
    </S.ControlsStyled>
  );
};
