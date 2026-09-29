/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { PublicTransportTypeEnum, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripStore, TTariffPublic } from 'stores/Trip/Trip.interface';

import { minTicketQuantity, tariffWasNotFound, tripsInfoTitle } from '../../constants';
import { CityFormItem } from './tripTypes/CityFormItem';
import { TravelCardFormItem } from './tripTypes/TravelCardFormItem';

interface WithTariffsProps {
  compensationType: string;
  form: FormInstance;
  field: FormListFieldData;
  tripStore: ITripStore;
  logger: ILogger;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
  currentTariff?: TTariffPublic;
}

export const WithTariffs: FC<WithTariffsProps> = observer(
  ({
    compensationType, form, field, tripStore, logger, updateGeneralSum, currentTransportTypes,
  }) => {
    const [tripCost, setTripCost] = React.useState(0);
    const [sum, setSum] = React.useState(0);

    const currentTariff = tripStore.actualTariff;

    const currentDate = new Date();
    currentDate.setMonth(currentDate.getMonth() + 1);

    const updateFieldData = (trip: any, quantity: number, price: number): void => {
      // eslint-disable-next-line no-param-reassign
      trip.sum = quantity * price;
      // eslint-disable-next-line no-param-reassign
      trip.quantity = quantity;
      // eslint-disable-next-line no-param-reassign
      trip.cost = price;
      // FIXME no-param-reassign
      updateGeneralSum();
    };
    const updateSum = (currentField: FormListFieldData, quantity: string | number | null, price: number): void => {
      const tripsInfo = form.getFieldValue(tripsInfoTitle);
      const trip = tripsInfo[currentField.key];
      if (typeof quantity === 'number') {
        setSum(quantity * price);
        updateFieldData(trip, quantity, price);
      } else if (trip) {
        updateFieldData(trip, minTicketQuantity, tripCost);
      }
    };

    const updateFinancialPerformance = (ticketCost: number, currentField: FormListFieldData): void => {
      setTripCost(ticketCost);
      updateSum(currentField, minTicketQuantity, ticketCost);
    };

    const updateCityTripCompensationData = (
      selectedTransportType: SelectValue,
      currentField: FormListFieldData
    ): void => {
      let ticketCost: number;

      switch (selectedTransportType) {
        case PublicTransportTypeEnum.CITY_BUS: {
          ticketCost = currentTariff?.busAvailability ? currentTariff.busTicketCost : 0;
          break;
        }
        case PublicTransportTypeEnum.CITY_METRO: {
          ticketCost = currentTariff?.metroAvailability ? currentTariff.metroTicketCost : 0;
          break;
        }
        case PublicTransportTypeEnum.CITY_TRAM: {
          ticketCost = currentTariff?.tramAvailability ? currentTariff.tramTicketCost : 0;
          break;
        }
        case PublicTransportTypeEnum.CITY_TROLLEYBUS: {
          ticketCost = currentTariff?.trolleybusAvailability ? currentTariff.trolleybusTicketCost : 0;
          break;
        }
        case PublicTransportTypeEnum.CITY_TRAIN: {
          ticketCost = currentTariff?.cityLocalTrainAvailability ? currentTariff.cityLocalTrainCost : 0;
          break;
        }
        default:
          ticketCost = 0.0;
      }

      updateFinancialPerformance(ticketCost, currentField);
    };

    const updateTravelCardCompensationData = (
      selectedTransportType: SelectValue,
      currentField: FormListFieldData
    ): void => {
      let ticketCost;

      switch (selectedTransportType) {
        case PublicTransportTypeEnum.TRAVEL_CARD_ALL_CITY_TRANSPORT: {
          ticketCost = currentTariff?.travelCardAllCityTransportAvailability
            ? currentTariff.travelCardAllCityTransportCost
            : 0;
          break;
        }
        case PublicTransportTypeEnum.TRAVEL_CARD_BUS: {
          ticketCost = currentTariff?.travelCardBusAvailability ? currentTariff.travelCardBusCost : 0;
          break;
        }
        case PublicTransportTypeEnum.TRAVEL_CARD_METRO: {
          ticketCost = currentTariff?.travelCardMetroAvailability ? currentTariff.travelCardMetroCost : 0;
          break;
        }
        case PublicTransportTypeEnum.TRAVEL_CARD_TRAIN: {
          ticketCost = currentTariff?.travelCardLocalTrainAvailability ? currentTariff.travelCardLocalTrainCost : 0;
          break;
        }
        case PublicTransportTypeEnum.TRAVEL_CARD_TRAM: {
          ticketCost = currentTariff?.travelCardTramAvailability ? currentTariff.travelCardTramCost : 0;
          break;
        }
        case PublicTransportTypeEnum.TRAVEL_CARD_TROLLEYBUS: {
          ticketCost = currentTariff?.travelCardTrolleybusAvailability ? currentTariff.travelCardTrolleybusCost : 0;
          break;
        }
        default:
          ticketCost = 0.0;
      }
      const tripsInfo = form.getFieldValue(tripsInfoTitle);
      const trip = tripsInfo[currentField.key];
      // eslint-disable-next-line no-mixed-operators
      updateFieldData(trip, 1, ticketCost);
      updateFinancialPerformance(ticketCost, currentField);
    };

    const handleTransportTypeChange = (selectedTransportType: SelectValue, currentField: FormListFieldData): void => {
      // FIXME sonarjs/cognitive-complexity

      if (currentTariff) {
        if (compensationType === TransportCompensations.CITY_TRIP_COMPENSATION) {
          updateCityTripCompensationData(selectedTransportType, currentField);
        }

        if (compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION) {
          updateTravelCardCompensationData(selectedTransportType, currentField);
        }
      } else {
        logger.toMessage('error', tariffWasNotFound);
      }
    };

    const getCityItem = (): JSX.Element => (
      <CityFormItem
        form={form}
        field={field}
        tripCost={tripCost}
        sum={sum}
        updateSum={updateSum}
        handleTransportTypeChange={handleTransportTypeChange}
        currentTransportTypes={currentTransportTypes}
      />
    );

    const getTravelCardItem = (): JSX.Element => (
      <TravelCardFormItem
        handleTransportTypeChange={handleTransportTypeChange}
        form={form}
        field={field}
        currentTransportTypes={currentTransportTypes}
        spareDate={currentDate}
        tripCost={tripCost}
        updateSum={updateSum}
      />
    );

    const getItem = (): JSX.Element => {
      const isTravelCardTrip = compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION;
      return isTravelCardTrip ? getTravelCardItem() : getCityItem();
    };

    return <>{getItem()}</>;
  }
);
