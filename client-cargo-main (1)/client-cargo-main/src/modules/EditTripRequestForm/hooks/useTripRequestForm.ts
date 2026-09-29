import { useCallback, useEffect } from 'react';
import { FormInstance, useForm } from 'antd/lib/form/Form';
import moment from 'moment';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';
import { buildAddressString } from 'utils/waypoint/core';

export type TripFieldsType = Record<string, any>;

const getWaypointsField = (
  waypoints: WaypointModel[]
): {
  waypoint: string;
  waitTime: number;
}[] => waypoints.map(waypoint => ({
  waypoint: buildAddressString(waypoint),
  waitTime: waypoint.waitTime,
}));

export const useTripRequestForm = ({
  tripRequestRoute,
  request,
  initialValues,
}: {
  tripRequestRoute: TripRequestRoute;
  request: TripRequestModel;
  initialValues?: () => TripFieldsType;
}): {
  form: FormInstance<any>;
  initialValues: TripFieldsType;
} => {
  // FIXME make it without selfStore
  const { [StoreNames.selfStore]: selfStore } = useAppStoreContext();

  const [form] = useForm();
  const { actualRoute } = tripRequestRoute;

  const passenger = request?.passenger.id === selfStore.selfEmployee.id ? 'me' : 'notme';

  const defaultInitialValues: () => TripFieldsType = useCallback(
    () => ({
      waypoints: getWaypointsField(actualRoute.waypoints),
      passenger,
      employee: request.passenger.fullNameWithCode,
      coopTrip: false,
      when: 'notnow',
      taxiClass:
        (request.taxiClass && TaxiClassEnum[request?.taxiClass])
        || (request.transportType && TransportTypeEnum[request?.transportType]),
      passengerCount: request.passengerCount,
      date: moment(request.desiredDate),
      purpose: request.purpose.id,
      commentary: request.commentForDriver,
      preferences: request?.requestOptions || [],
    }),
    [actualRoute.waypoints, request, passenger]
  );

  const getInitialValues: () => TripFieldsType = initialValues || defaultInitialValues;

  useEffect(() => {
    form.setFieldsValue({ waypoints: getWaypointsField(actualRoute.waypoints) });
  }, [form, actualRoute.waypoints]);

  return {
    form,
    initialValues: getInitialValues(),
  };
};
