
import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import { AvailablePublicTTCompensationType, AvailablePublicTransportType } from 'api/trip-requests';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TTariffPublic } from 'stores/Trip/Trip.interface';

import { CompensationTypeSelect } from '../additional/CompensationTypeSelect';
import { WithFile } from './compensationByFile/controller';
import { WithTariffs } from './compensationByTariff/controller';

export interface CityTripItemProps {
  form: FormInstance;
  field: FormListFieldData;
  updateGeneralSum: () => void;
  availableTransportTypes: AvailablePublicTransportType[];
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  index: number;
  currentTariff?: TTariffPublic;
}

export const TripItems: FC<CityTripItemProps> = observer(
  ({
    form, field, updateGeneralSum, availableTransportTypes, availablePublicCompensation, index, currentTariff,
  }) => {
    const [compensationType, setCompensationType] = React.useState<string>(
      TransportCompensations.CITY_TRIP_COMPENSATION
    );

    // TODO resetFields - делаем reset всех полей той карточки, у которой сменился тип компенсации
    //  карточек в одной заявке может быть очень много. Об этом надо помнить. обнулять нужно только для той, для которой
    //  сменили тип компенсации
    const resetFields = (selectedIndex: number, selectedCompensationType: string): void => {
      const oldArray = form.getFieldValue('tripsInfo');
      oldArray[selectedIndex] = { ticketCount: 1, compensationType: selectedCompensationType };
    };
    const [transportTypes, setTransportTypes] = React.useState<LabeledValue[]>([]);

    useEffect(() => {
      const tt: LabeledValue[] = availableTransportTypes
        .filter(x => x.publicCompensationType === compensationType)
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

      if (isSuburbTrip) {
        return (
          <WithFile
            form={form}
            field={field}
            tripStore={tripStore}
            logger={logger}
            updateGeneralSum={updateGeneralSum}
            currentTransportTypes={transportTypes}
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
        />
      );
    };

    return (
      <>
        <CompensationTypeSelect
          field={field}
          compensationType={compensationType}
          setCompensationType={setCompensationType}
          availablePublicCompensation={availablePublicCompensation}
          index={index}
          resetFields={resetFields}
        />
        {getItem()}
      </>
    );
  }
);
