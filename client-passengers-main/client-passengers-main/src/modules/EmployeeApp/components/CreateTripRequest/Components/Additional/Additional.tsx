
import React, { FC, useEffect, useState } from 'react';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { AdditionalProps } from '../../types/types';
import { AdditionalCoopTrip } from './AdditionalCoopTrip';
import { IndicationPassengers } from '../IndicationPassengers/IndicationPassengers';

export const Additional: FC<AdditionalProps> = ({
  form,
  transport,
  coopTripData,
  submitDisabled,
  onCommonFinish,
  disabledFields,
  personalCar,
  step,
  disabled,
  productTransportType,
  selectedQuantity,
  setSelectedQuantity,
  passengerCount,
}) => {
  const [activeCollapsePanel, setActiveCollapsePanel] = useState<string[]>([]);
  const [isCoopTrip, setIsCoopTrip] = useState(true);

  const {
    TAXI, PERSONAL, CARSHARING,
  } = TransportTypeEnum;
  const {
    BUSINESS, COMFORT, ECONOMY,
  } = TaxiClassEnum;

  const commonTransportType = transport?.split('-')[0];
  const isPersonalTransportType = commonTransportType === PERSONAL;
  const hasOwnTransport = !!personalCar;
  const isPersonalTransportNotACar = personalCar?.transportType !== 'CAR';
  const isPersonalCar = !!form.getFieldValue('personalCar');
  const isDisabledSwitch
    = (isPersonalTransportType && !isPersonalCar)
    || !!disabledFields?.includes('coopTrip');

  useEffect(() => {
    const isCoopTripTypeSelected
      = commonTransportType
      && [TAXI, PERSONAL, CARSHARING, BUSINESS, COMFORT, ECONOMY].includes(commonTransportType as TaxiClassEnum);

    if (isCoopTripTypeSelected) {
      setIsCoopTrip(true);
      setActiveCollapsePanel(['2']);
    } else {
      setIsCoopTrip(false);
      form.setFieldsValue({
        coopTrip: false,
      });
      setActiveCollapsePanel([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [BUSINESS, COMFORT, ECONOMY, PERSONAL, TAXI, commonTransportType, transport]);

  // co-op trip on personal transport type only available for a car
  useEffect(() => {
    if (isPersonalTransportType && hasOwnTransport && isPersonalTransportNotACar) {
      setIsCoopTrip(false);
    }
  }, [commonTransportType, hasOwnTransport, isPersonalTransportNotACar, isPersonalTransportType]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const toggleAdditionals = (value: string | string[]): void => activeCollapsePanel.length === 1 ? setActiveCollapsePanel([]) : setActiveCollapsePanel([value[0]]);

  return (
    <>
      <AdditionalCoopTrip
        form={form}
        coopTripData={coopTripData}
        isCoopTrip={isCoopTrip}
        setIsCoopTrip={setIsCoopTrip}
        submitDisabled={submitDisabled}
        switchDisabled={isDisabledSwitch}
        tabsDisabled={!!disabledFields?.includes('coopTrip')}
        onCommonFinish={onCommonFinish}
        step={step}
        disabled={disabled}
        productTransportType={productTransportType}
        selectedQuantity={selectedQuantity}
        setSelectedQuantity={setSelectedQuantity}
      />
      {step === 3 && isCoopTrip && passengerCount
      && (productTransportType === TransportTypeEnum.CARSHARING
      || productTransportType === TransportTypeEnum.PERSONAL
      || productTransportType === TransportTypeEnum.TAXI)
      && (
      <IndicationPassengers
        passengerCount={passengerCount}
        form={form}
        productTransportType={productTransportType}
      />
      )}
    </>
  );
};
