import React, { FC, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import ErrorBoundary from 'antd/lib/alert/ErrorBoundary';
import { observer } from 'mobx-react';

import { useGetRoute, useUpdateDetailedViewRoute, useUpdateRouteStatus, useUpdateRouteV2 } from 'api/planner';
import { useForm } from 'antd/lib/form/Form';

import { RouteStatusEnum } from '../../types';
import { usePlanner } from '../../context/PlannerContext';

import { colors } from "shared/styles/styles";
import SpinWrapped from "shared/components/SpinWrapped/SpinWrapped";
import { useAppStoreContext } from "shared/hooks/useAppStoreContext";

import { PlanningCreatedModal } from '../Modals/PlanningCreatedModal/PlanningCreatedModal';
import { Header } from './Header/Header';
import { Addresses } from './Addresses/Addresses';

import { MainInfo } from "./MainInfo";
import { TabsStyled } from './RouteDetailed.style';

import * as S from '../styles.common';

const RouteDetailed: FC = observer(() => {
  const [visible, setVisible] = useState(false);

  const [form] = useForm();
  const { plannerStore } = useAppStoreContext();
  const { id } = useParams();
  const {
    routeIdDetailed,
    setCoordinates,
    setMarkers,
    setRouteIdDetailed,
    setRouteMode
  } = usePlanner();
  const routeId = id || routeIdDetailed;

  const { data, isFetching } = useGetRoute(routeId);
  const { draggedOrder, clearCheckedListStore } = plannerStore;

  const [updateRouteV2] = useUpdateRouteV2();
  const [updateRouteStatus] = useUpdateRouteStatus();
  const [updateDetailedRoute] = useUpdateDetailedViewRoute(routeId);

  const {
    humanReadableId, desiredDate, waypoints, cost, weight, distance, volume, status, segments,
  } = data;

  const routeCoordinates = waypoints?.map(point => ({
    longitude: point.address.longitude as number,
    latitude: point.address.latitude as number,
  }));

  const markers = waypoints?.map(point => ({
    longitude: point.address.longitude,
    latitude: point.address.latitude,
    city: point.address.city,
    street: point.address.street,
    house: point.address.house,
  }));

  useEffect(() => {
    setMarkers(markers)
  }, [waypoints])

  useEffect(() => {
    setCoordinates(
      segments && segments?.length > 0
        ? segments.flatMap(segment => segment.coordinates || [])
        : routeCoordinates
      );
  }, [waypoints, segments])

  const submitPlanning = () => {
    updateRouteStatus({ routelistId: routeId, status: RouteStatusEnum.CARGO_PLANNING_FINISHED }).then(data => {
      if (data?.id) {
        setVisible(true);
      }
    });
  };

  const changeStatus = () => {
    updateRouteStatus({ routelistId: routeId, status: RouteStatusEnum.CARGO_PLANNING });
  };

  const handleOkModal = () => {
    setVisible(false);
  };

  const handleCancel = () => {
    setVisible(false);
  };

  const handleBackToListRoutes = () => {
    setVisible(false);
    setRouteMode('list');
  };

  return (
    <ErrorBoundary>
      <S.PlannerStyled>
        <S.MainStyled
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
            const newRoute = plannerStore.dragOrderToRoute(
              routeId,
              plannerStore.routesListStore.content,
              draggedOrder,
              plannerStore.checkedOrdersListStore
            );

            if (plannerStore.isDateInRange && !!newRoute) {
              const ordersToAdd = plannerStore.checkedOrdersListStore.length > 0
                ? plannerStore.checkedOrdersListStore
                : (draggedOrder ? [draggedOrder] : []);

              updateRouteV2({routeListId: routeId, requests: ordersToAdd.map(order => order.id) }).then(() => {
                handleCancel();
                plannerStore.clearRouteStore();
                clearCheckedListStore();
                setRouteIdDetailed(routeId);
                setRouteMode('detailed');
              });
            }
            e.currentTarget.style.borderColor = colors.gray3;
          }}
        >
          <S.ContainerStyled>
            <Header
              humanReadableId={humanReadableId}
              desiredDate={desiredDate}
              submitPlanning={submitPlanning}
              formValues={{ ...form.getFieldsValue() }}
              status={status}
              changeStatus={changeStatus}
            />
            <TabsStyled>
              <MainInfo
                updateRoute={updateDetailedRoute}
                isLoading={isFetching}
                form={form}
                route={data}
                distance={distance}
                volume={volume}
                weight={weight}
                cost={cost}
              />
              <S.Wrapper>
                <Addresses data={data} routeId={routeId} updateRoute={updateDetailedRoute} />
              </S.Wrapper>
              {/* key 2 убран в задаче 42757 */}
            </TabsStyled>
          </S.ContainerStyled>
        </S.MainStyled>
        <PlanningCreatedModal
          visible={visible}
          handleOkModal={handleOkModal}
          handleBackToListRoutes={handleBackToListRoutes}
          onCancel={handleCancel}
        />
      </S.PlannerStyled>
    </ErrorBoundary>
  );
});

export default RouteDetailed;
