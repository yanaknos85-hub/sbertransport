import { Form, Skeleton } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';
import React, { useEffect } from 'react';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import DefaultCar from '../../../../../../shared/components/Images/evaluationIcons/defaultCar.svg';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { CoopTrip, FormValues } from '../../types/types';
import styles from './styles.module.scss';
import SuitableTripsItem from './SuitableTripsItem';

export const SuitableTrips = ({
  form,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  disabled,
  coopTripData,
  onCommonFinish,
  step,
  productTransportType,
  selectedQuantity,
}: {
  form: FormInstance;
  disabled?: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (
    data: FormValues,
    isTripSearching?: boolean,
    isSuitabelTrip?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ) => void;
  step?: number;
  productTransportType?: TransportTypeEnum;
  selectedQuantity?: string;
}): JSX.Element => {
  const {
    suitableCooperativeTrips, isSearchingSuitableTrip, searchingMessage,
  } = coopTripData;

  const formValues = form.getFieldsValue();
  // eslint-disable-next-line no-nested-ternary
  const isSuitablePage = step
    ? productTransportType === TransportTypeEnum.TAXI || productTransportType === TransportTypeEnum.PERSONAL
      ? step === 3
      : step === 4
    : true;
  const { passengerCount } = form.getFieldsValue();

  useEffect(
    () => {
      if (
        isSuitablePage
        && !isSearchingSuitableTrip
        && passengerCount === (parseInt(formValues.passengerCount, 10) || 1)
      ) {
        onCommonFinish(form.getFieldsValue(), true, true);
      }
    },
    // ! do not change these dependencies in useEffect array
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [formValues.date, formValues.purpose, formValues.isReadyToOrder, step, selectedQuantity, formValues.taxiClass]
  );

  return (
    <div className={classNames('cooperative-trip', styles.cooperativeTripWrapper)}>
      <Form.Item name="coopTripId">
        {suitableCooperativeTrips.length === 0 && !isSearchingSuitableTrip ? (
          <div className={styles.noSuitableTrips}>
            <span className={styles.noSuitableTripsTitle}>
              {CreateRequestLinksTitles[CreateRequestLinks.noSuitableTripsTitle]}
            </span>
            <span className={styles.noSuitableTripsDescription}>{searchingMessage()}</span>
            <div className={styles.imgDefaultCar}>
              <img src={DefaultCar} alt="" />
            </div>
          </div>
        ) : (
          <div className={styles.noSuitableTrips}>
            <span className={styles.noSuitableTripsTitle}>
              {CreateRequestLinksTitles[CreateRequestLinks.suitableTrips]}
            </span>
            <span className={styles.noSuitableTripsDescription}>{searchingMessage()}</span>
          </div>
        )}

        {isSearchingSuitableTrip ? (
          <Skeleton active={true} />
        ) : (
          <SuitableTripsItem
            onCommonFinish={onCommonFinish}
            form={form}
            coopTripData={coopTripData}
          />
        )}
      </Form.Item>
    </div>
  );
};
