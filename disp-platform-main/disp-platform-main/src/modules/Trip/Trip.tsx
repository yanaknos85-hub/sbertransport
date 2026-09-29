import React, { FC } from 'react';
import { Col } from 'antd';
import { useHistory, useParams } from 'react-router-dom';

import Panel from 'components/Panel/Panel';
import { StatusSelect } from 'components/StatusSelect';
import { RowWithContainer } from 'components/Row/Row';
import PageHeader from 'components/PageHeader/PageHeader';
import Divider from 'components/Divider/Divider';
import withSuspense from 'components/withSuspense';

import { useProfile } from 'api/profile/profile.api';
import { useEditTrip } from 'api/trips/trips.api';

import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';
import { useAppStore } from 'ioc';
import { TRIP_STATUSES, TaxiClass } from 'constants/trips.constants';
import { TripTypes } from 'constants/app.constants';

import { TripInfoProvider, useTripInfo } from './context/TripInfo.context';
import { ROW_GUTTER } from './constants/passTrip.constants';

import { RouteCard } from './sections/RouteCard/RouteCard';
import { DataCard } from './sections/DataCard/DataCard';
import { TransferDataCard } from './sections/TransferDataCard/TransferDataCard';
import { AdditionalDataCard } from './sections/AdditionalDataCard/AdditionalDataCard';
import { ChangeLogCard } from './sections/ChangeLogCard/ChangeLogCard';
import { VehicleCard } from './sections/VehiceCard';
import { DriverCard } from './sections/DriverCard';
import { PassengersCard } from './sections/PassengersCard/Passengers';
import { CommentCard } from './sections/CommentCard';

import styles from './Trip.module.scss';

const Page: FC<{ tripId: UUID }> = withSuspense(({ tripId }) => {
  const history = useHistory();

  const { logger } = useAppStore();

  const { trip } = useTripInfo();

  const { contractorId } = useProfile().data;

  const [editTrip, { isLoading }] = useEditTrip(contractorId);

  const changeStatus = (value: TRIP_STATUSES) => {
    editTrip({
      tripId,
      data: [{ field: 'status', value }],
    })
      .then(() => logger.toMessage('success', 'Статус успешно изменен'))
      .catch(ignore);
  };

  return (
    <>
      <Panel>
        <PageHeader
          title={`Поездка ${trip.humanReadableId}`}
          onBack={history.goBack}
          ghost={false}
          extra={(
            <StatusSelect
              tripMode={TripTypes.Passenger}
              isEdit
              loading={isLoading}
              value={trip.status}
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              onChange={changeStatus as any}
              className={styles.statusSelect}
            />
          )}
        />

        <Divider />

        <RouteCard />
      </Panel>

      <DataCard />

      {trip.taxiClass === TaxiClass.GROUP_TRANSFER && (
        <>
          <TransferDataCard />
          <AdditionalDataCard />
        </>
      )}

      <RowWithContainer align="stretch" gutter={ROW_GUTTER}>
        <Col span={12}>
          <VehicleCard vehicle={trip.vehicle ?? trip.planned?.vehicle} />
        </Col>
        <Col span={12}>
          <DriverCard />
        </Col>
      </RowWithContainer>

      <PassengersCard />

      <CommentCard requests={trip.requests} comment={trip.comment} />

      <ChangeLogCard />
    </>
  );
});

const Trip: FC = withSuspense(() => {
  const { id: tripId } = useParams<{ id: UUID }>();

  return (
    <TripInfoProvider tripId={tripId}>
      <Page tripId={tripId} />
    </TripInfoProvider>
  );
});

export default Trip;
