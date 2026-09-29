import React from 'react';
import { useParams } from 'react-router-dom';

import { useGetOrder } from 'api/planner';
import { useTranslation } from 'i18n';
import { TabPane } from 'shared/components/Tab/TabPane';
import { OrderDetailedListItem } from './OrderDetailedListItem/OrderDetailedListItem';
import { Header } from './Header/Header';
import { Tools } from '../RouteDetailed/Tools/Tools';

import { TabsStyled } from './OrderDetailed.styles';
import { RouteInformation } from '../RouteInformation';
import { LatLngTuple } from '../../types';
import { OrderData } from './OrderDetailed.types';

import * as S from '../styles.common';

interface RouteParams {
  id: string;
};

/* TODO Выглядит будто не используется, проверить */
const OrderDetailed = () => {
  const { id } = useParams<RouteParams>();
  const { t } = useTranslation();
  // const outerMapRef = useRef<Map>(null);

  const { data } = useGetOrder(id);

  const { humanReadableId, desiredDate, listCargo, waypoints, segments, calculatedTariff } = data as unknown as OrderData;

  let totalWeight = 0;
  let totalVolume = 0;

  listCargo.forEach((cargo) => {
      totalWeight += cargo.weight;
      totalVolume += cargo.volume;
    });

  const markers = waypoints.map((point) => ({
    longitude: point.longitude,
    latitude: point.latitude,
    city: point.city,
    street: point.street,
    house: point.house,
  }));

  const coordinates = waypoints
    ?.filter((point) => point.latitude || point.longitude)
    .map((point) => ({ latitude: point.latitude, longitude: point.longitude }));

  const latitude = segments?.length > 1 ? segments[0]?.coordinates[0]?.latitude : coordinates[0]?.latitude;
  const longitude = segments?.length > 1 ? segments[0]?.coordinates[0]?.longitude : coordinates[0]?.longitude;
  const isValidCoordinates = latitude !== undefined && longitude !== undefined;
  const position: LatLngTuple = isValidCoordinates ? [latitude, longitude] : [0, 0];

  return (
    <S.PlannerStyled>
      <S.CardWrapper>
        <S.ContainerStyled>
          <Header humanReadableId={humanReadableId} desiredDate={desiredDate} />
          <TabsStyled defaultActiveKey="1">
            <TabPane /* Скрыто в рамках задачи 16249
              tab={t.Planner.routeTitle} key="1" */
            >
              <RouteInformation distance={calculatedTariff.distance} volume={totalVolume} weight={totalWeight} cost={calculatedTariff.cost} />
              <S.Wrapper>
                <OrderDetailedListItem list={waypoints} />
              </S.Wrapper>
            </TabPane>
            {/* Скрыто в рамках задачи 16249
           <TabPane tab={t.Planner.execution} key="2">
              {t.Planner.onDevelopment}
            </TabPane> */}
          </TabsStyled>
        </S.ContainerStyled>
        <Tools />
        <S.MapContainer>
          {/* was multi */}
          <S.Map
            position={position}
            markers={markers}
            polylines={segments.length > 1 ? segments : [{ coordinates }]}
            dragging
            zoomControl
            fitToShowAllGeometry
          />
        </S.MapContainer>
      </S.CardWrapper>
    </S.PlannerStyled>
  );
};

export default OrderDetailed;
