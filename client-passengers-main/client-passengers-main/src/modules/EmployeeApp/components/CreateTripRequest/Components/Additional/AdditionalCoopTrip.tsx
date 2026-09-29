/* eslint-disable @typescript-eslint/no-explicit-any */

import {
  Form, FormInstance, Switch, Tabs
} from 'antd';
import React, {
  Dispatch, SetStateAction, useEffect, useState
} from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { CoopTrip } from '../../types/types';
import { SuitableTrips } from '../SuitableTrips';
import '../../styles/override.scss';

const { TabPane } = Tabs;

export const AdditionalCoopTrip = ({
  form,
  isCoopTrip,
  setIsCoopTrip,
  coopTripData,
  switchDisabled,
  tabsDisabled,
  submitDisabled,
  onCommonFinish,
  step,
  productTransportType,
  selectedQuantity,
  setSelectedQuantity,
}: {
  form: FormInstance;
  isCoopTrip: boolean;
  setIsCoopTrip: Dispatch<SetStateAction<boolean>>;
  switchDisabled: boolean;
  tabsDisabled: boolean;
  submitDisabled: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (
    data: any,
    isTripSearching?: boolean,
    isSuitabelTrip?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ) => void;
  step?: number;
  // eslint-disable-next-line react/no-unused-prop-types
  disabled?: boolean;
  productTransportType?: TransportTypeEnum;
  selectedQuantity?: string;
  setSelectedQuantity?: React.Dispatch<React.SetStateAction<string>>;
}): JSX.Element => {
  const [placesQuantity, setPlacesQuantity] = useState([
    { label: 'Одно', value: 1 },
    { label: 'Два', value: 2 },
    { label: 'Три', value: 3 },
    { label: 'Четыре', value: 4 },
  ]);

  const handleQuantityChange = (value: string) => {
    if (setSelectedQuantity) {
      setSelectedQuantity(value);
    }

    form.setFieldsValue({
      passengerCount: Number(value),
    });
  };

  const handleSelectedQuantity = (value: boolean) => {
    setIsCoopTrip(value);
    form.setFieldsValue({
      coopTrip: value,
    });
  };

  useEffect(() => {
    return () => {
      form.setFieldsValue({
        passengerCount: 1,
      });
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (productTransportType === TransportTypeEnum.PERSONAL || productTransportType === TransportTypeEnum.CARSHARING) {
      setPlacesQuantity(prevItems => {
        const hasNumberFive = prevItems.some(item => item.value === 5);

        if (!hasNumberFive) {
          return [...prevItems, { label: 'Пять', value: 5 }];
        }

        return prevItems;
      });
    } else {
      setPlacesQuantity(prevItems => prevItems.filter(item => item.value !== 5));
    }
  }, [productTransportType]);

  useEffect(() => {
    if (!isCoopTrip && setSelectedQuantity) {
      setSelectedQuantity('1');
      form.setFieldsValue({
        passengerCount: 1,
      });
    }
  }, [isCoopTrip]);

  return (
    <>
      <Form.Item noStyle={true} shouldUpdate={true}>
        {(): JSX.Element | null => (
          <>
            {productTransportType !== TransportTypeEnum.CARSHARING && (
              <Form.Item>
                <SuitableTrips
                  form={form}
                  disabled={submitDisabled}
                  coopTripData={coopTripData}
                  onCommonFinish={onCommonFinish}
                  step={step}
                  productTransportType={productTransportType}
                  selectedQuantity={selectedQuantity}
                />
              </Form.Item>
            )}
            <Form.Item name="coopTrip">
              <div className="titleTripType">{CreateRequestLinksTitles[CreateRequestLinks.tripType]}</div>
              <div className="switchTripType">
                <div>
                  <Switch
                    defaultChecked={isCoopTrip}
                    checked={isCoopTrip}
                    onChange={handleSelectedQuantity}
                    disabled={switchDisabled}
                  />
                  <span>{CreateRequestLinksTitles[CreateRequestLinks.cooperateType]}</span>
                </div>
                {productTransportType === TransportTypeEnum.PERSONAL && (
                  <div className="amountPerPassenger">
                    <span>{CreateRequestLinksTitles[CreateRequestLinks.amountPerPassenger]}</span>
                  </div>
                )}
              </div>
            </Form.Item>
            <Form.Item name="passengerCount">
              {productTransportType !== 'BUS' && isCoopTrip && (
                <div className="passengerCountTrip">
                  <span>
                    {productTransportType === TransportTypeEnum.TAXI
                      ? CreateRequestLinksTitles[CreateRequestLinks.numberSeatsTripsTitleForTaxi]
                      : CreateRequestLinksTitles[CreateRequestLinks.numberSeatsTripsTitle]}
                  </span>
                  <Tabs activeKey={selectedQuantity} onChange={handleQuantityChange}>
                    {placesQuantity.map(i => (
                      <TabPane
                        tab={i.value}
                        key={i.value}
                        disabled={tabsDisabled}
                      />
                    ))}
                  </Tabs>
                </div>
              )}
            </Form.Item>
          </>
        )}
      </Form.Item>
    </>
  );
};
