import { Divider, FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, { useEffect, useState } from 'react';
import type {
  Dispatch,
  FC,
  SetStateAction
} from 'react';
import type {
  AvailablePublicTTCompensationType,
  AvailablePublicTransportType,
  PublicApprovals
} from 'api/trip-requests';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { PublicInfoCard, PublicTransportTypeEnum, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TTariffPublic } from 'stores/Trip/Trip.interface';

import { CompensationTypeSelect } from '../additional/CompensationTypeSelect';
import { WithFile } from './compensationByFile/controller';
import { WithTariffs } from './compensationByTariff/controller';
import AboutTariffs from './AboutTariffs/AboutTariffs';
import { tripsInfoTitle } from '../constants';

export interface CityTripItemProps {
  form: FormInstance;
  field: FormListFieldData;
  updateGeneralSum: () => void;
  availableTransportTypes: AvailablePublicTransportType[];
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  index: number;
  currentTariff?: TTariffPublic;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  publicApprovals: PublicApprovals | null;
  remove: (index: number | number[]) => void;
}

export const TripItems: FC<CityTripItemProps> = observer(
  ({
    form,
    field,
    updateGeneralSum,
    availableTransportTypes,
    availablePublicCompensation,
    index,
    currentTariff,
    setIsSaveCompensation,
    publicApprovals,
    remove,
  }) => {
    const [compensationType, setCompensationType] = useState<TransportCompensations>(
      TransportCompensations.CITY_TRIP_COMPENSATION
    );
    const [availableCompensation, setAvailableCompensation] = useState<AvailablePublicTTCompensationType[]>(availablePublicCompensation);
    const checkCityTripCompensation = (currentTariff?.metroAvailability === false || currentTariff?.metroTicketCost === 0)
      && (currentTariff?.busAvailability === false || currentTariff?.busTicketCost === 0)
      && (currentTariff?.tramAvailability === false || currentTariff?.tramTicketCost === 0)
      && (currentTariff?.trolleybusAvailability === false || currentTariff?.trolleybusTicketCost === 0);

    useEffect(() => {
      setCompensationType(form.getFieldValue('tripsInfo')[index].compensationType);
    }, []);

    // TODO resetFields - делаем reset всех полей той карточки, у которой сменился тип компенсации
    //  карточек в одной заявке может быть очень много. Об этом надо помнить. обнулять нужно только для той, для которой
    //  сменили тип компенсации
    const resetFields = (selectedIndex: number, selectedCompensationType: string): void => {
      const oldArray = form.getFieldValue('tripsInfo');
      oldArray[selectedIndex] = { ticketCount: 1, compensationType: selectedCompensationType };
    };
    const [transportTypes, setTransportTypes] = useState<LabeledValue[]>([]);

    useEffect(() => {
      const tt: LabeledValue[] = availableTransportTypes
        .filter(x => {
          if (compensationType === TransportCompensations.CITY_TRIP_COMPENSATION) {
            return (x.name === PublicTransportTypeEnum.CITY_METRO && (currentTariff?.metroAvailability === true && currentTariff?.metroTicketCost !== 0))
              || (x.name === PublicTransportTypeEnum.CITY_BUS && (currentTariff?.busAvailability === true && currentTariff?.busTicketCost !== 0))
              || (x.name === PublicTransportTypeEnum.CITY_TROLLEYBUS && (currentTariff?.trolleybusAvailability === true && currentTariff?.trolleybusTicketCost !== 0))
              || (x.name === PublicTransportTypeEnum.CITY_TRAM && (currentTariff?.tramAvailability === true && currentTariff?.tramTicketCost !== 0));
          }
          return x.publicCompensationType === compensationType;
        }
        )
        .map(
          x => ({
            label: x.rusName,
            value: x.name,
          } as LabeledValue)
        );
      setTransportTypes(tt);
      updateGeneralSum();
    }, [compensationType, availableTransportTypes, updateGeneralSum]);

    const { [StoreNames.tripStore]: tripStore, logger } = useAppStoreContext();

    const getItem = (): JSX.Element => {
      const isSuburbTrip = compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION;
      const isPaidServices = compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION;

      if (isSuburbTrip || isPaidServices) {
        return (
          <WithFile
            form={form}
            field={field}
            tripStore={tripStore}
            logger={logger}
            updateGeneralSum={updateGeneralSum}
            currentTransportTypes={transportTypes}
            publicApprovals={publicApprovals}
            setIsSaveCompensation={setIsSaveCompensation}
            index={index}
            compensationType={compensationType}
          />
        );
      }
      return (
        <WithTariffs
          compensationType={compensationType}
          form={form}
          field={field}
          tripStore={tripStore}
          logger={logger}
          updateGeneralSum={updateGeneralSum}
          currentTransportTypes={transportTypes}
          currentTariff={currentTariff}
          index={index}
          setIsSaveCompensation={setIsSaveCompensation}
          publicApprovals={publicApprovals}
        />
      );
    };

    useEffect(() => {
      if (transportTypes.length < 1
        && compensationType === TransportCompensations.CITY_TRIP_COMPENSATION
        && checkCityTripCompensation
      ) {
        const indexCityTrip = availablePublicCompensation.findIndex(v => v.name === TransportCompensations.CITY_TRIP_COMPENSATION);
        const modifiedAvailableCompensation = indexCityTrip !== -1 ? availablePublicCompensation.splice(indexCityTrip, 1) : availablePublicCompensation;
        setCompensationType(TransportCompensations.SUBURB_TRIP_COMPENSATION);
        form.setFieldsValue({
          tripsInfo: form
            .getFieldValue(tripsInfoTitle)
            .map((el: PublicInfoCard) => el.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION ? { ...el, compensationType: TransportCompensations.SUBURB_TRIP_COMPENSATION } : el
            ),
        });
        setAvailableCompensation(modifiedAvailableCompensation);
      } else {
        setAvailableCompensation(availablePublicCompensation);
      }
    }, [transportTypes]);

    return (
      <>
        {index > 0 && <Divider />}
        <CompensationTypeSelect
          field={field}
          form={form}
          compensationType={compensationType}
          setCompensationType={setCompensationType}
          availablePublicCompensation={availableCompensation}
          index={index}
          resetFields={resetFields}
          setIsSaveCompensation={setIsSaveCompensation}
          remove={remove}
          updateGeneralSum={updateGeneralSum}
        />
        {getItem()}
        {compensationType === TransportCompensations.CITY_TRIP_COMPENSATION && (
          <AboutTariffs />
        )}
      </>
    );
  }
);
