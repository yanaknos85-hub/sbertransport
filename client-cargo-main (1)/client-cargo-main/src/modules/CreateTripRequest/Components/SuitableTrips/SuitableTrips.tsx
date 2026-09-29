import React, { useEffect } from 'react';
import { Form, Radio, Skeleton } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';

import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import { CoopTrip, FormValues } from '../../types/types';
import { SuitableTripView } from './SuitableTripView';

import styles from './styles.module.scss';

export const SuitableTrips = ({
  form,
  disabled,
  coopTripData,
  onCommonFinish,
}: {
  form: FormInstance;
  disabled: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (data: FormValues, isTripSearching?: boolean, currentJoiningTrip?: TripSuitableModel) => void;
}): JSX.Element => {
  const {
    suitableCooperativeTrips, isSearchingSuitableTrip, searchingMessage,
  } = coopTripData;

  const suitableTripsView = (): JSX.Element[] => suitableCooperativeTrips.map(item => (
    <SuitableTripView
      key={item.id}
      item={item}
      form={form}
      onCommonFinish={onCommonFinish}
    />
  ));

  const formValues = form.getFieldsValue();

  useEffect(
    () => {
      if (!disabled) {
        onCommonFinish(form.getFieldsValue(), true);
      }
    },
    // ! do not change these dependencies in useEffect array
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [formValues.date, formValues.purpose, formValues.taxiClass, formValues.passengerCount]
  );

  return (
    <div className={classNames('cooperative-trip', styles.cooperativeTripWrapper)}>
      <Form.Item name="coopTripId">
        <Radio.Group defaultValue={suitableTripsView()}>
          {isSearchingSuitableTrip ? <Skeleton active={true} /> : <>{suitableTripsView()}</>}
        </Radio.Group>
        {suitableCooperativeTrips.length === 0 && !isSearchingSuitableTrip && searchingMessage()}
      </Form.Item>
    </div>
  );
};
