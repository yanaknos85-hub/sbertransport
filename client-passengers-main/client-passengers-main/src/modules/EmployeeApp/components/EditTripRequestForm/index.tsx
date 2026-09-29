/* eslint-disable @typescript-eslint/no-explicit-any */

import { Button, Form } from 'antd';
import classNames from 'classnames';
import { Moment } from 'moment';
import React from 'react';

import useCurrentCoordinates from 'shared/hooks/geo/useCurrentCoordinates';
import { useTripRequestRoute } from 'shared/hooks/trip';
import { ActualRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';

import { Additional } from '../CreateTripRequest/Components/Additional/Additional';
import { useCooperativeTrip } from '../CreateTripRequest/hooks/useCooperativeTrip';
import { useTransport } from '../CreateTripRequest/hooks/useTransport';
import { TransportTypes } from '../EditTripRequest/TransportTypes';
import {
  TripRequestDate, TripRequestMap, TripRequestPurpose, TripRequestRoute
} from './components';
import { useTripRequestDisable, useTripRequestForm, useTripRequestPurpose } from './hooks';
import { useTripPurposeList } from './hooks/useTripPurposeList';

import styles from './styles.module.scss';

export interface TripRequestFormValues {
  expected: ActualRoute;
  coopTrip: boolean;
  coopTripId?: string;
  date: Moment;
  passenger: 'me' | 'notme';
  passengerCount: number;
  preferences: string[];
  purpose: string;
  taxiClass?: TransportTypeEnum;
  waypoints: { waypoint: string; waitTime: any }[];
  when: 'notnow' | 'now';
  tariffId: string;
  employee: string;
  commentary: string;
}

export type TripRequestField = 'date' | 'purpose' | 'coopTrip' | 'preferences' | 'passenger' | 'personalCar';

const EditTripRequestForm: React.FC<{
  onSubmit: (values: TripRequestFormValues) => void;
  onValuesChange?: (changedValues: TripRequestFormValues, allValues: TripRequestFormValues) => void;
  request: TripRequestModel;
  disabledFields?: TripRequestField[];
  submitDisabled?: boolean;
}> = ({
  onSubmit, onValuesChange, request, disabledFields, submitDisabled,
}) => {
  const tripRequestRoute = useTripRequestRoute(request);
  const currentCoordsState = useCurrentCoordinates();
  const { form, initialValues } = useTripRequestForm({ tripRequestRoute, request });

  const tripPurposeList = useTripPurposeList(request);

  const tripPurpose = useTripRequestPurpose(request.purpose.id);

  const coopTripData = useCooperativeTrip({ tripPurposeList });

  const { transport, setTransport } = useTransport(request.transportType);
  const { disabled } = useTripRequestDisable({
    transport,
    tripPurpose,
    tariffsCosts: tripRequestRoute.tariffsCosts,
    request,
  });

  const submitIsDisabled = submitDisabled || disabled;

  return (
    <div className={classNames(styles.formWrapper)}>
      <Form
        layout="vertical"
        form={form}
        name="editTripRequest"
        size="middle"
        onFinish={onSubmit}
        initialValues={initialValues}
        onValuesChange={onValuesChange}
        className={styles.form}
      >
        <TripRequestMap tripRequestRoute={tripRequestRoute} currentCoordsState={currentCoordsState} />
        <div className={classNames(styles.formContent)}>
          <TripRequestRoute
            tripRequestRoute={tripRequestRoute}
            currentCoordsState={currentCoordsState}
            form={form}
          />
          <TripRequestDate
            purposes={tripPurposeList.purposes}
            tripPurpose={tripPurpose}
            form={form}
            disabled={disabledFields?.includes('date') || false}
          />
          <TripRequestPurpose
            tripPurposeList={tripPurposeList}
            tripPurpose={tripPurpose}
            form={form}
            disabled={disabledFields?.includes('purpose') || false}
          />
          <TransportTypes
            tariffsInfo={tripRequestRoute.tariffsCosts}
            request={request}
            form={form}
            purpose={form.getFieldValue('purpose')}
            setCurrentTransportType={setTransport}
            // eslint-disable-next-line @typescript-eslint/no-empty-function
            setProduct={() => {}}
          />
          <Additional
            form={form}
            submitDisabled={submitIsDisabled}
            coopTripData={coopTripData as any}
            onCommonFinish={onSubmit}
            disabledFields={disabledFields}
          />
          <Button
            type="primary"
            htmlType="submit"
            size="middle"
            className={styles.submit}
            block={true}
            disabled={submitDisabled}
          >
            Сохранить
          </Button>
        </div>
      </Form>
    </div>
  );
};

export default EditTripRequestForm;
