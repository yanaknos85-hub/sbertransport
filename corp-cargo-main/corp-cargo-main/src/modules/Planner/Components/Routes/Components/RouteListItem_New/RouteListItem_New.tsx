import React, { FC, useEffect, useState } from 'react';
import { Checkbox, Divider, Tooltip } from 'antd';
import moment from 'moment';
import { colors } from 'shared/styles/styles';
import { observer } from 'mobx-react';
import { useUpdateRouteV2 } from 'api/planner';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { usePlanner } from '../../../../context/PlannerContext';
import { StatusLabel } from '../../../StatusLabel/StatusLabel';
import { CREATOR_INDICATORS, PLANNING_INDICATORS } from '../../constants';
import {
  AutoType,
  ContractorInfoType,
  CoordinatesType,
  CreatorTypeEnum,
  RouteStatusType,
  RouteType,
  RouteWaypointType,
  Segment,
} from '../../../../types';
import { RouteAddressesList } from '../RouteAddressesList/RouteAddressesList';
import { RouteParameters_New } from '../../../RouteDetailed/RouteParams_New/RouteParameters_New';
import CopyIdIcon from '../../../OrderDetailed/CopyId';
import { getRouteCoordinates } from '../../../utils';
import * as S from './RouteListItem_New.styles';

interface Props {
  id: string;
  humanReadableId: string;
  transportType: string;
  desiredDate: string;
  waypoints: RouteWaypointType[];
  segments?: Segment[];
  isAddToRoute?: boolean;
  handleOpenConfirmModal?: () => void;
  status: RouteStatusType;
  weight: number;
  volume: number;
  handleWaypoints?: (waypoints: RouteWaypointType[], id: string) => void;
  handleChange?: (e: CheckboxChangeEvent) => void;
  active: boolean;
  auto: AutoType;
  creatorType: CreatorTypeEnum;
  contractorInfo: ContractorInfoType;
}

export const RouteListItem_New: FC<Props> = observer(({ id: routeId, ...props }) => {
  const {
    humanReadableId,
    transportType,
    desiredDate,
    waypoints,
    isAddToRoute,
    status,
    handleOpenConfirmModal,
    weight,
    volume,
    handleWaypoints,
    handleChange,
    active,
    auto,
    creatorType,
    contractorInfo,
    segments,
  } = props;

  const [checkedItem, setCheckedItem] = useState(false);

  const { t } = useTranslation();

  const {
    checkedListRoutes,
    setCheckedListRoutes,
    setRouteMode,
    setRouteIdDetailed,
    setCoordinates
  } = usePlanner();

  const { plannerStore } = useAppStoreContext();

  const [updateRouteV2] = useUpdateRouteV2();

  const { draggedOrder } = plannerStore;

  const onChange = (e: CheckboxChangeEvent, id: string) => {
    handleChange && handleChange(e);
    setCheckedItem(e.target.checked);

    if (e.target.checked) {
      const routesForHandleSend = plannerStore.routesListStore.content.find(route => route.id === id);
      setCheckedListRoutes(prev => [...prev, routesForHandleSend as RouteType]);
    } else {
      setCheckedListRoutes(() => [...checkedListRoutes.filter(item => item.id !== id)]);
    }
  };

  const handleAddToRoute = async () => {
    plannerStore.handleChooseRoute(routeId, plannerStore.routesListStore.content);
    if (plannerStore.isDateInRange) {
      await updateRouteV2({
        routeListId: routeId,
        requests: plannerStore.checkedOrdersListStore.map(order => order.id) 
      });
      plannerStore.clearRouteStore();
      plannerStore.clearCheckedListStore();
      setRouteIdDetailed(routeId);
      setRouteMode('detailed');
    }
  };

  const handleClick = () => {
    handleWaypoints?.(waypoints, routeId);
    setCoordinates(
      segments && segments.length > 0
        ? segments.reduce<CoordinatesType[]>((acc, el) => {
            if (el.coordinates) {
              acc.push(...(el.coordinates || []));
            }
            return acc;
          }, [])
        : getRouteCoordinates(waypoints)
    );
  };

  useEffect(() => {
    setCheckedItem(!!checkedListRoutes?.find(item => item.id === routeId));
  }, [checkedListRoutes]);

  return (
    <S.ListItem
      draggable={false}
      onDragOver={(e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.currentTarget.style.borderColor = colors.green10;
      }}
      onDragLeave={(e: React.DragEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.currentTarget.style.borderColor = colors.gray3;
      }}
      onDrop={(e: React.DragEvent<HTMLDivElement>) => {
        plannerStore.dragOrderToRoute(
          routeId,
          plannerStore.routesListStore.content,
          draggedOrder,
          plannerStore.checkedOrdersListStore
        );
        if (handleOpenConfirmModal && plannerStore.isDateInRange) {
          handleOpenConfirmModal();
        }
        e.currentTarget.style.borderColor = colors.gray3;
      }}
      onClick={handleClick}
      active={active}
    >
      <StatusLabel status={creatorType} indicators={CREATOR_INDICATORS} />
      <StatusLabel status={status} indicators={PLANNING_INDICATORS} />
      <S.Header>
        <S.HeaderContent>
          <S.Delivery>
            {t.Planner.delivery}
            {' '}
            {moment(desiredDate).format('DD.MM.YYYY')}
          </S.Delivery>
          <S.Id>
            <span
              onClick={e => {
                e.stopPropagation();
                setRouteIdDetailed(routeId);
                setRouteMode('detailed');
              }}
              className='id'
            >
              {humanReadableId}
            </span>
            <CopyIdIcon id={humanReadableId} tooltipText={t.Planner.copyRouteId} />
          </S.Id>
          <Tooltip title={contractorInfo?.name}>
            <S.Contractor>
              {contractorInfo?.name}
            </S.Contractor>
          </Tooltip>
        </S.HeaderContent>
        <RouteParameters_New
          weight={weight}
          volume={volume}
          autoCapacity={auto?.capacity?.capacity}
          autoVolume={auto?.volume}
        />
        {!isAddToRoute && (
          <S.Checkbox onClick={e => e.stopPropagation()}>
            <Checkbox
              checked={checkedItem}
              onChange={(e: CheckboxChangeEvent) => onChange(e, routeId)}
            />
          </S.Checkbox>
        )}
      </S.Header>
      <Divider style={{ margin: ' 4px 0' }} />
      <RouteAddressesList waypoints={waypoints} transportType={transportType} />
      <S.ListItemInfoStyled>
        {/* TODO Уточнить по реализации drag and drop и добавления маршрута */}
        {isAddToRoute && (
          <>
            <S.ButtonStyled onClick={() => setRouteMode('list')}>{t.Planner.cancel}</S.ButtonStyled>
            <S.ButtonStyled success onClick={handleAddToRoute}>{t.Planner.choose}</S.ButtonStyled>
          </>
        )}
      </S.ListItemInfoStyled>
    </S.ListItem>
  );
});
