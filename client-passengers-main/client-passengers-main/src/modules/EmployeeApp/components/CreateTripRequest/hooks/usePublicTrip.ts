/* eslint-disable no-unsafe-optional-chaining */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { FormInstance, useForm } from 'antd/lib/form/Form';
import { Dispatch, SetStateAction, useState } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import {
  PublicTripCompensation,
  SavedFileInfo,
  TTariffPublic,
  TransportCompensation
} from 'stores/Trip/Trip.interface';
import { AvailablePublicTTCompensationType, AvailablePublicTransportType } from 'api/trip-requests';
import { getTimeZone } from '../utils/utils';
import { useModalState } from 'shared/hooks/useModal';
import { errorEmptyFields, FormItemNames } from '../Components/PublicCars/constants';
import moment, { Moment } from 'moment';
import { getSavingData } from '../Components/PublicCars/additional/creatingDataUtils';
import { DATE_FORMAT, SYSTEM_MESSAGES } from 'constants/constants.app';
import { getMinCostTaxi } from 'utils/getMinCostTaxi';

interface CreateTripeRequestHook {
  childForm: FormInstance;
  isSaveCompensation: boolean;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  okHandler: () => void;
  generalSum: number;
  setGeneralSum: Dispatch<SetStateAction<number>>;
}

export const usePublicTrip = (form: FormInstance): CreateTripeRequestHook => {
  const [childForm] = useForm();
  const [isSaveCompensation, setIsSaveCompensation] = useState(false);
  const { purpose } = form?.getFieldsValue();
  const [generalSum, setGeneralSum] = useState<number>(0);
  const [, { hide }] = useModalState();
  const date: Moment = form?.getFieldValue('date') || moment();

  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.selfStore]: selfStore,
    logger,
  } = useAppStoreContext();

  const createStringDate = (selectedDate: string | number | Date | Moment): string => {
    return moment(selectedDate).format(DATE_FORMAT.BASE);
  };

  const getSameElementTravelCompensation = (newArray: any[], selectedElement: any): any => newArray.find(y => {
    if (y.calendar) {
      const y0 = createStringDate(y.calendar[0]);
      const y1 = createStringDate(y.calendar[1]);
      const x0 = createStringDate(selectedElement.calendar[0]);
      const x1 = createStringDate(selectedElement.calendar[1]);

      return (
        y.compensationType === selectedElement.compensationType
        && y.publicTransportType === selectedElement.publicTransportType
        && JSON.stringify(y0) === JSON.stringify(x0)
        && JSON.stringify(y1) === JSON.stringify(x1)
      );
    }
    return false;
  });

  const getSameElementCityCompensation = (newArray: any[], selectedElement: any): any => newArray.find(
    y => y.compensationType === selectedElement.compensationType
    && y.publicTransportType === selectedElement.publicTransportType
  );

  const getArrayForTariff = (array: any[], newArray: any[], selectedElement: any): void => {
    const findElem
      = selectedElement.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
        ? getSameElementTravelCompensation(newArray, selectedElement)
        : getSameElementCityCompensation(newArray, selectedElement);

    if (!findElem) {
      newArray.push(selectedElement);
    } else {
      const index = newArray.indexOf(findElem);
      if (selectedElement.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].ticketCount += selectedElement.ticketCount;
      }

      if (
        selectedElement.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION
        || selectedElement.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION
      ) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].cost += selectedElement.cost;
      }

      if (selectedElement.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].ticketCount += selectedElement.ticketCount;
        // eslint-disable-next-line no-param-reassign
        newArray[index].cost += selectedElement.cost;
      }
    }
  };

  const getArrayWithoutDuplicates = (array: PublicInfoCard[]): any[] => {
    const newArray: PublicInfoCard[] = [];
    array.forEach(x => {
      if (
        x.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION
        || x.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION
      ) {
        newArray.push(x);
      }

      if (
        x.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
        || x.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION
      ) {
        getArrayForTariff(array, newArray, x);
      }
    });
    return newArray;
  };

  const getSavedFileData = (array: any[]): (SavedFileInfo | null)[] => {
    return array.map(file => (file.savedFileData ? file.savedFileData : null));
  };

  const getTransportTypeCompensationList = (
    items: PublicInfoCard[],
    transportTypes: AvailablePublicTransportType[],
    compensationTypes: AvailablePublicTTCompensationType[]
  ): TransportCompensation[] => items.map(item => ({
    compensationType: compensationTypes.find(elem => elem.name === item.compensationType),
    transportType: transportTypes.find(y => y.name === item.publicTransportType),
    ticketsCost: item.cost,
    ticketsCount: item.ticketCount,
    ticketsExpirationStart:
      item.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION && item.calendar
        ? createStringDate(item.calendar[0])
        : undefined,
    ticketsExpirationEnd:
      item.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION && item.calendar
        ? createStringDate(item.calendar[1])
        : undefined,
  }));

  const getData = (array: PublicInfoCard[], payRequestId: string | undefined): PublicTripCompensation => {
    const purposeObj = tripStore.purposesMapped[purpose];
    const {
      employee, passenger, commentForPurpose,
    } = form?.getFieldsValue();

    const formPassenger = passenger === 'me' ? selfStore.selfEmployee : employee;
    const author = selfStore.selfEmployee;

    const arrayWithoutDuplicates: PublicInfoCard[] = getArrayWithoutDuplicates(array);
    const savedFileData = getSavedFileData(arrayWithoutDuplicates);

    const transportTypes: AvailablePublicTransportType[]
      = childForm.getFieldValue(FormItemNames.transportTypes) ?? [];
    const compensationTypes: AvailablePublicTTCompensationType[]
      = childForm.getFieldValue(FormItemNames.compensationTypes) ?? [];
    const transportCompensation: TransportCompensation[] = getTransportTypeCompensationList(
      arrayWithoutDuplicates,
      transportTypes,
      compensationTypes
    );
    const transportCompensationDocumentID = transportCompensation.map((item, index) => {
      const itemFile = savedFileData[index];
      return itemFile ? { ...item, attachedDocumentId: itemFile.id } : item;
    });

    const finishedSavedFileData = savedFileData.filter(item => item !== null);
    const minTariffTaxi = getMinCostTaxi(tripStore.classCosts);
    const savingData = getSavingData(
      // @ts-ignore
      formPassenger || selfStore.selfEmployee,
      author,
      purposeObj,
      generalSum,
      date,
      geo,
      transportCompensationDocumentID,
      finishedSavedFileData as SavedFileInfo[],
      (tripStore.actualTariff as TTariffPublic)?.id,
      payRequestId,
      minTariffTaxi,
      commentForPurpose
    );

    return savingData.author.id === savingData.passenger.id ? { ...savingData, timeZone: getTimeZone() } : savingData;
  };

  const saveCompensationRequest = (array: PublicInfoCard[], payRequestId: string | undefined): Promise<unknown> => {
    const newData = getData(array, payRequestId);

    if (newData.expected?.cost === 0) {
      return new Promise((resolve, reject) => {
        reject(new Error(SYSTEM_MESSAGES.tripRequestCostZero));
      });
    }

    return tripStore.savePublicTripRequest(newData);
  };

  const cancelHandler = (): void => {
    // eslint-disable-next-line react/prop-types
    form?.resetFields();
    childForm.resetFields();
    childForm.setFieldsValue({ tripsInfo: [{ ticketCount: 1 }] });
    setGeneralSum(0);
    hide();
  };

  const searchTripNumber = (tripsInfo: PublicInfoCard[]) => {
    for (const item of tripsInfo) {
      if (item.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION) {
        if (item.humanReadableId) {
          return item.humanReadableId.value;
        }
      }
    }
  };

  const okHandler = (): void => {
    childForm.validateFields().then(
      () => {
        // eslint-disable-next-line no-shadow
        const tripsInfo = JSON.parse(JSON.stringify(childForm.getFieldValue(FormItemNames.tripsInfo)));

        tripStore.setProgressApplicationCreationRequest(true);

        tripsInfo.forEach((el: PublicInfoCard, index: number) => {
          if (
            el.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION
            || el.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION
            || el.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
          ) {
            tripsInfo[index].cost *= 100;
          }
        });

        const payRequestId = searchTripNumber(tripsInfo);

        // FIXME no-shadow
        if (geo.calculatedRoute) {
          setIsSaveCompensation(true);
          saveCompensationRequest(tripsInfo, payRequestId)
            .then(() => {
              cancelHandler();
              tripStore.setProgressApplicationCreationRequest(false);
            })
            .catch(error => {
              if (error.response && error.response.status) {
                logger.toMessage(
                  'error',
                  error.response.status === 409
                    ? 'На данный вид транспорта, в указанном периоде, уже есть проездной билет'
                    : error.response.data.message
                );
              } else {
                logger.toMessage('error', error.message);
              }
              tripStore.setProgressApplicationCreationRequest(false);
            });
        }
      },
      () => {
        logger.toMessage('error', errorEmptyFields);
      }
    );
  };

  return {
    childForm,
    isSaveCompensation,
    setIsSaveCompensation,
    okHandler,
    generalSum,
    setGeneralSum,
  };
};
