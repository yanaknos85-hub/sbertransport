import { useState } from 'react';
import moment, { Moment } from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { ITripTariff, TaxiClassEnum, TTripRequestNew } from 'stores/Trip/Trip.interface';

import { TripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import { TransportTypesConfig } from '../../TaxiClasses/TaxiClassesConfig';
import { CreateRequestLinks, CreateRequestLinksTitles } from '../constants/CreateRequest.constants';
import { FormValues } from '../types/types';
import { getTariffByTaxiClass, getTimeZone } from '../utils/utils';

export interface CoopTrip {
  applyCoopTrip: (
    data: FormValues,
    isTripSearching: boolean,
    coopTripId: string,
    tariffsInfo: ITripTariff[],
    currentJoiningTrip?: TripSuitableModel
  ) => void | undefined;
  searchingMessage: () => string;
  suitableCooperativeTrips: TripSuitableModel[];
  isSearchingSuitableTrip: boolean;
  isSearchedSuitableTrip: boolean;
}

export const useCooperativeTrip = ({ tripPurposeList }: { tripPurposeList: TripPurposeList }): CoopTrip => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.limitsStore]: limitsStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();
  // Создали state для присвоения совместным поездкам типа транспорта чтобы потом отображать их тариф
  const [suitableCooperativeTrips, setSuitableCooperativeTrips] = useState<TripSuitableModel[]>([]);
  const {
    searchCoopTrips, isSearchingSuitableTrip, joinCoopTrip, isSearchedSuitableTrip, clearCoopTrips,
  } = tripStore;
  const searchTripsForBoth = async (
    trip: TTripRequestNew,
    tariffsInfo: ITripTariff[],
    taxiClass: TaxiClassEnum
  ): Promise<void> => {
    const taxiTripReq = {
      ...trip,
      transportType: TransportTypeEnum.TAXI,
      timeZone: getTimeZone(),
      ...(taxiClass !== 'PERSONAL'
        ? {
          taxiClass,
          tariffId: getTariffByTaxiClass(tariffsInfo, taxiClass),
        }
        : {
          tariffId: getTariffByTaxiClass(tariffsInfo, TaxiClassEnum.ECONOMY),
        }),
    };

    const personalTripReq = {
      ...trip,
      transportType: TransportTypeEnum.PERSONAL,
      tariffId: getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL),
      timeZone: getTimeZone(),
    };
    try {
      // @ts-ignore
      await searchCoopTrips(taxiTripReq);
      const taxiTrips = tripStore.suitableCooperativeTrips.map(el => ({
        ...el,
        type: TransportTypeEnum.TAXI,
        taxiClass: TaxiClassEnum.ECONOMY,
      }));
      // @ts-ignore
      await searchCoopTrips(personalTripReq);
      const personalTrips = tripStore.suitableCooperativeTrips.map(el => ({
        ...el,
        type: TransportTypeEnum.PERSONAL,
      }));
      setSuitableCooperativeTrips([...taxiTrips, ...personalTrips] as TripSuitableModel[]);
    } catch (error) {
      // @ts-ignore
      // eslint-disable-next-line no-console
      console.error(error.message);
    }
  };

  const createRequestObject = ({
    data,
    tariffsInfo,
    currentJoiningTrip,
  }: {
    data: FormValues;
    tariffsInfo: ITripTariff[];
    currentJoiningTrip?: TripSuitableModel;
  }) => {
    const {
      coopTrip, employee, commentary, taxiClass, passenger: formPassenger, purpose, passengerCount,
    } = data;

    const taxiClassNormalized = taxiClass?.split('-')[0] as TaxiClassEnum;
    const transportType = TransportTypesConfig[taxiClassNormalized]?.transportType;

    const isTaxi = transportType === TransportTypeEnum.TAXI;
    const isPersonal = transportType === TransportTypeEnum.PERSONAL;

    const date: Moment = data.date || moment();
    const passenger
      = formPassenger === 'me'
        ? selfStore.selfEmployee
        : employeeStore.employeeListByOrg.find(x => x.fullNameWithCode === employee);

    const searchTripsDefaultTrip = {
      author: selfStore.selfEmployee,
      passenger: passenger ?? selfStore.selfEmployee,
      transportType,
      passengerCount: passengerCount || 1,
      tariffId: isPersonal
        ? getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
        : getTariffByTaxiClass(tariffsInfo, taxiClassNormalized),
      desiredDate: date.valueOf(),
      expected: geo.calculatedRoute,
      purpose: tripPurposeList?.getById(purpose),
      approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
      coopTrip,
      commentForDriver: commentary,
    };

    // if we search for a suitable trip then currentJoiningTrip is not being used,
    // but if we are joining the trip we use this object to get proper data about the trip
    const currentTripReq = (currentJoiningTrip
      ? {
        author: currentJoiningTrip.employeePassengers[0],
        passenger: passenger ?? selfStore.selfEmployee,
        transportType: currentJoiningTrip.isPersonal ? TransportTypeEnum.PERSONAL : transportType,
        ...(isTaxi && { taxiClass: TransportTypesConfig[taxiClassNormalized]?.taxiClass as TaxiClassEnum }),
        passengerCount: passengerCount || 1,
        tariffId: currentJoiningTrip.isPersonal
          ? getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
          : getTariffByTaxiClass(tariffsInfo, taxiClassNormalized),
        desiredDate: date.valueOf(),
        expected: geo.calculatedRoute,
        purpose: tripPurposeList?.getById(purpose),
        approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
        coopTrip,
        commentForDriver: commentary,
      }
      : searchTripsDefaultTrip) as unknown as TTripRequestNew;

    return {
      isTaxi,
      coopTrip,
      taxiClassNormalized,
      currentTripReq,
    };
  };

  const applyCoopTrip = (
    data: FormValues,
    isTripSearching: boolean,
    coopTripId: string,
    tariffsInfo: ITripTariff[],
    currentJoiningTrip?: TripSuitableModel
  ): void | undefined => {
    const {
      coopTrip, taxiClassNormalized, currentTripReq,
    } = createRequestObject({
      data,
      tariffsInfo,
      currentJoiningTrip,
    });

    if (geo.calculatedRoute && limitsStore.currentLimit && coopTrip) {
      if (isTripSearching) {
        searchTripsForBoth(
          currentTripReq,
          tariffsInfo,
          TransportTypesConfig[taxiClassNormalized]?.taxiClass as TaxiClassEnum
        );
      } else {
        joinCoopTrip(coopTripId, currentTripReq);
        clearCoopTrips();
      }
    }
  };

  const searchingMessage = (): string => isSearchedSuitableTrip
    ? CreateRequestLinksTitles[CreateRequestLinks.noSuitableTrips]
    : CreateRequestLinksTitles[CreateRequestLinks.noSuitableTripsSearch];
  return {
    suitableCooperativeTrips,
    applyCoopTrip,
    isSearchingSuitableTrip,
    isSearchedSuitableTrip,
    searchingMessage,
  };
};
