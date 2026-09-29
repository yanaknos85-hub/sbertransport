/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useEffect
} from 'react';

import { PublicTransportTypeEnum, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripStore, TTariffPublic, TransportCompensation } from 'stores/Trip/Trip.interface';

import { minTicketQuantity, tariffWasNotFound, tripsInfoTitle } from '../../constants';
import { CityFormItem } from './tripTypes/CityFormItem';
import { TravelCardFormItem } from './tripTypes/TravelCardFormItem';
import uuid from 'utils/uuid';
import { getBase64 } from '../../additional/fileUtils/fileUploadUtils';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { PublicApprovals } from 'api/trip-requests';
import { FileCompensation, FileStatus } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';

interface WithTariffsProps {
  compensationType: string;
  form: FormInstance;
  field: FormListFieldData;
  tripStore: ITripStore;
  logger: ILogger;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
  currentTariff?: TTariffPublic;
  index: number;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  publicApprovals: PublicApprovals | null;
}

export const WithTariffs: FC<WithTariffsProps> = observer(
  ({
    compensationType,
    form,
    field,
    tripStore,
    logger,
    updateGeneralSum,
    currentTransportTypes,
    index,
    setIsSaveCompensation,
    publicApprovals,
  }) => {
    const [tripCost, setTripCost] = React.useState(0);
    const [sum, setSum] = React.useState(0);
    const folder = uuid();
    const [loading, setLoading] = React.useState<boolean>(false);
    const [file, setFile] = React.useState<FileCompensation>();

    useEffect(() => {
      setFile(form.getFieldValue(tripsInfoTitle)[index].ticket?.file);
    }, []);

    const handleLoadFileChange = (fileCompensation: FileCompensation): void => {
      if (fileCompensation.status === FileStatus.UPLOADING) {
        setLoading(true);
        return;
      }
      if (fileCompensation.status === FileStatus.DONE) {
        setFile(fileCompensation);
        tripStore.saveFile(fileCompensation.originFileObj, folder).then(data => {
          const tripsInfo = form.getFieldValue(tripsInfoTitle);
          const trip = tripsInfo[field.key];
          trip.savedFileData = data;
          tripStore.setIsSaveFile(false);
          logger.toMessage('info', SYSTEM_MESSAGES.fileUploadSuccess);
        })
          .catch(() => {
            tripStore.setIsSaveFile(true);
            logger.toMessage('error', SYSTEM_MESSAGES.fileUploadError);
          });
        getBase64(fileCompensation.originFileObj, () => {
          setLoading(false);
        });
      }
    };

    const currentTariff = tripStore.actualTariff;

    const currentDate = new Date();
    currentDate.setMonth(currentDate.getMonth() + 1);

    const updateFieldData = (trip: TransportCompensation, quantity: number, price: number): void => {
      // eslint-disable-next-line no-param-reassign
      trip.sum = quantity * price;
      // eslint-disable-next-line no-param-reassign
      trip.quantity = quantity;
      // eslint-disable-next-line no-param-reassign
      trip.cost = price;
      // FIXME no-param-reassign
      updateGeneralSum();
    };

    const updateSum = (i: number, quantity: string | number | null, price: number, typeChange: string): void => {
      const tripsInfo = form.getFieldValue(tripsInfoTitle);
      const trip = tripsInfo[i];

      if (typeof quantity === 'number') {
        if (typeChange === 'increase') {
          setSum((quantity + 1) * price);
          updateFieldData(trip, quantity + 1, price);
        } else if (typeChange === 'decrease') {
          if (quantity <= 1) {
            updateFieldData(trip, minTicketQuantity, tripCost);
          } else {
            setSum((quantity - 1) * price);
            updateFieldData(trip, quantity - 1, price);
          }
        } else {
          setSum(quantity * price);
          updateFieldData(trip, minTicketQuantity, price);
        }
      } else if (trip) {
        updateFieldData(trip, minTicketQuantity, tripCost);
      }
    };

    const updateFinancialPerformance = (ticketCost: number, i: number): void => {
      setTripCost(ticketCost);
      updateSum(i, minTicketQuantity, ticketCost, '');
    };

    const updateCityTripCompensationData = (selectedTransportType: SelectValue, i: number): void => {
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

      updateFinancialPerformance(ticketCost, i);
    };

    const updateTravelCardCompensationData = (selectedTransportType: SelectValue, i: number): void => {
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
      const trip = tripsInfo[i];
      // eslint-disable-next-line no-mixed-operators
      updateFieldData(trip, 1, ticketCost);
      updateFinancialPerformance(ticketCost, i);
    };

    const handleTransportTypeChange = (selectedTransportType: SelectValue, i: number): void => {
      // FIXME sonarjs/cognitive-complexity
      if (currentTariff) {
        if (compensationType === TransportCompensations.CITY_TRIP_COMPENSATION) {
          updateCityTripCompensationData(selectedTransportType, i);
        }

        if (compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION) {
          updateTravelCardCompensationData(selectedTransportType, i);
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
        index={index}
        setIsSaveCompensation={setIsSaveCompensation}
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
        updateGeneralSum={updateGeneralSum}
        index={index}
        setIsSaveCompensation={setIsSaveCompensation}
        handleLoadFileChange={handleLoadFileChange}
        file={file}
        logger={logger}
        setFile={setFile}
        loading={loading}
        publicApprovals={publicApprovals}
      />
    );

    const getItem = (): JSX.Element => {
      const isTravelCardTrip = compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION;
      return isTravelCardTrip ? getTravelCardItem() : getCityItem();
    };

    return <>{getItem()}</>;
  }
);
