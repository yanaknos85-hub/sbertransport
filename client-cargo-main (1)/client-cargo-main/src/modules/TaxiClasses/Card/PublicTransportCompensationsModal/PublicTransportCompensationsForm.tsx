import React, { Dispatch, FC, SetStateAction } from 'react';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';

import { AvailablePublicTransportType, AvailablePublicTTCompensationType } from 'api/trip-requests';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TTariffPublic } from 'stores/Trip/Trip.interface';

import { tripsInfoTitle } from './Components/constants';
import { TripList } from './Components/TripList';

export const PublicTransportCompensationsForm: FC<{
  formRef: FormInstance;
  generalSum: number;
  setGeneralSum: Dispatch<SetStateAction<number>>;
  availableTransportTypes: AvailablePublicTransportType[];
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  currentTariff?: TTariffPublic;
}> = ({
  formRef, generalSum, setGeneralSum, availableTransportTypes, availablePublicCompensation, currentTariff,
}) => {
  const getSum = (array: any[], compensationType: TransportCompensations, fieldTitle: string): number => array
    .filter((x: any) => x.compensationType === compensationType && (x.cost || x.sum))
    .reduce((total: number, element: any) => total + element[fieldTitle], 0);

  const updateGeneralSum = (): void => {
    const formTripsInfo = formRef.getFieldValue(tripsInfoTitle);
    const citySum = getSum(formTripsInfo, TransportCompensations.CITY_TRIP_COMPENSATION, 'sum');
    const suburbSum = getSum(formTripsInfo, TransportCompensations.SUBURB_TRIP_COMPENSATION, 'cost');
    const travelCardSum = getSum(formTripsInfo, TransportCompensations.TRAVEL_CARD_COMPENSATION, 'cost');
    const finalSum = citySum + suburbSum + travelCardSum;
    setGeneralSum(finalSum);
  };

  const getMainForm = (): JSX.Element => (
    <Form
      form={formRef}
      layout="vertical"
      name="create-request"
      size="middle"
    >
      <TripList
        form={formRef}
        generalSum={generalSum}
        updateGeneralSum={updateGeneralSum}
        availableTransportTypes={availableTransportTypes}
        availablePublicCompensation={availablePublicCompensation}
        currentTariff={currentTariff}
      />
    </Form>
  );

  return getMainForm();
};
