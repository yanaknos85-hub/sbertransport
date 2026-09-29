import React, { useState } from 'react';
import { Title } from '@sber-sbertransport/ui-kit/src';
import { Col, Row } from 'antd';

import withErrorBoundary from 'components/withErrorBoundary';
import { TRIP_STATUSES } from 'constants/trips.constants';

import { useTripInfo } from 'modules/Trip/context/TripInfo.context';

import Flex from 'components/Flex/Flex';
import TextButton from 'modules/Trip/components/TextButton/TextButton';

import RouteNullAlert from './components/RouteNullAlert/RouteNullAlert';
import Route from './components/Route/Route';
import Map from './components/Map/Map';
import SpoofingWarningBanner from './components/SpoofingWarningBanner/SpoofingWarningBanner';
import useSegments from './hooks/useSegments';

import styles from './RouteCard.module.scss';

/** Маршрут */
export const RouteCard = withErrorBoundary(() => {
  const [isOpened, setIsOpened] = useState(true);

  const toggleOpened = () => setIsOpened(prev => !prev);

  const { trip } = useTripInfo();

  const isFinishedTrip = trip.status === TRIP_STATUSES.ORDER_FINISHED;

  const {
    planSegments, factSegments, isFetching,
  } = useSegments(trip.id, isFinishedTrip);

  const isRouteNull = isFinishedTrip && !isFetching && !factSegments;
  const isSpoofing = isFinishedTrip && !isFetching && factSegments;

  return (
    <>
      <Flex
        justifyContent="space-between"
        alignItems={isRouteNull ? 'center' : 'flex-end'}
        marginBottom={isOpened}
      >
        <Title level={4} className={styles.cardTitle}>Маршрут</Title>
        {isSpoofing && <SpoofingWarningBanner />}
        {isRouteNull && <RouteNullAlert />}
        <TextButton onClick={toggleOpened}>{isOpened ? 'Скрыть' : 'Раскрыть'}</TextButton>
      </Flex>

      {isOpened && (
        <Row>
          <Col span={12}>
            <Route />
          </Col>
          <Col span={12}>
            <Map
              planSegments={planSegments}
              factSegments={factSegments}
              isFinishedTrip={isFinishedTrip}
            />
          </Col>
        </Row>
      )}
    </>
  );
});
