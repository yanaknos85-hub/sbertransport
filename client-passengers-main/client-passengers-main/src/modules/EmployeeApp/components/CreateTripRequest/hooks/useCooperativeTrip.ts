/* eslint-disable @typescript-eslint/no-explicit-any */

import moment, { Moment } from 'moment';
import { useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { ITripTariff, TTripRequestNew, TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { TripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import { TransportTypesConfig } from '../../TaxiClasses/TaxiClassesConfig';
import { CreateRequestLinks, CreateRequestLinksTitles } from '../constants/CreateRequest.constants';
import { FormValues } from '../types/types';
import {
  getOutcomeCostByTaxiClass,
  getOutcomeTariffByTaxiClass, getPriceByTaxiClass, getTariffByTaxiClass, getTimeZone
} from '../utils/utils';
import { getMinCostTaxi } from 'utils/getMinCostTaxi';

export interface CoopTrip {
  applyCoopTrip: (
    data: FormValues,
    isTripSearching: boolean,
    coopTripId: string | undefined,
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
    const checkTaxiClass
      = taxiClass !== TaxiClassEnum.TAXI && taxiClass !== undefined ? taxiClass : TaxiClassEnum.ECONOMY;
    const taxiTripReq = {
      ...trip,
      transportType: TransportTypeEnum.TAXI,
      timeZone: getTimeZone(),
      ...(taxiClass !== 'PERSONAL'
        ? {
          taxiClass,
          tariffId: getTariffByTaxiClass(tariffsInfo, checkTaxiClass),
          requestPrice: getPriceByTaxiClass(tariffsInfo, taxiClass),
          outcomeTariffId: getOutcomeTariffByTaxiClass(tariffsInfo, taxiClass),
        }
        : {
          tariffId: getTariffByTaxiClass(tariffsInfo, TaxiClassEnum.ECONOMY),
          requestPrice: getPriceByTaxiClass(tariffsInfo, TaxiClassEnum.ECONOMY),
          outcomeTariffId: getOutcomeTariffByTaxiClass(tariffsInfo, TaxiClassEnum.ECONOMY),
        }),
    };

    const personalTripReq = {
      ...trip,
      transportType: TransportTypeEnum.PERSONAL,
      tariffId: getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL),
      requestPrice: getPriceByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL),
      timeZone: getTimeZone(),
      outcomeTariffId: getOutcomeTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL),
    };
    try {
      // @ts-ignore
      await searchCoopTrips(taxiTripReq);
      const taxiTrips = tripStore.suitableCooperativeTrips.map(el => ({
        ...el,
        type: TransportTypeEnum.TAXI,
        // taxiClass: TaxiClassEnum.ECONOMY,
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
      coopTrip, employee, commentary, taxiClass, passenger: formPassenger, purpose, passengerCount, commentForPurpose,
    } = data;

    const taxiClassNormalized = taxiClass?.split('-')[0] as TaxiClassEnum;
    const transportType = TransportTypesConfig[taxiClassNormalized]?.transportType;

    const isTaxi = transportType === TransportTypeEnum.TAXI;
    const isPersonal = transportType === TransportTypeEnum.PERSONAL;

    const date: Moment = data.date || moment();
    const passenger
      = formPassenger === 'me'
        ? selfStore.selfEmployee
        : employeeStore.employeeListByOrg.find((x: any) => x.fullNameWithCode === employee);

    const searchTripsDefaultTrip = {
      author: selfStore.selfEmployee,
      passenger: passenger ?? selfStore.selfEmployee,
      transportType,
      passengerCount: passengerCount || 1,
      tariffId: isPersonal
        ? getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
        : getTariffByTaxiClass(tariffsInfo, taxiClassNormalized),
      desiredDate: date.valueOf(),
      expected: {
        ...geo.calculatedRoute,
        cost: getPriceByTaxiClass(tariffsInfo, taxiClass || TransportTypeEnum.PERSONAL),
        outcomeCost: currentJoiningTrip?.isPersonal
          ? getOutcomeCostByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
          : getOutcomeCostByTaxiClass(tariffsInfo, taxiClassNormalized),
      },
      purpose: tripPurposeList?.getById(purpose),
      approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
      coopTrip,
      commentForDriver: commentary,
      source: 'WEB',
      minTariffTaxi: getMinCostTaxi(tripStore.classCosts),
      commentForPurpose: commentForPurpose,
      outcomeTariffId: currentJoiningTrip?.isPersonal
        ? getOutcomeTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
        : getOutcomeTariffByTaxiClass(tariffsInfo, taxiClassNormalized),
    };

    const joiningTaxiClass = currentJoiningTrip?.taxiClass;
    const timeZone = currentJoiningTrip?.timeZone;
    // eslint-disable-next-line no-nested-ternary
    const reqTransportType = currentJoiningTrip?.isPersonal
      ? TransportTypeEnum.PERSONAL
      : joiningTaxiClass
        ? TransportTypeEnum.TAXI
        : transportType;

    // if we search for a suitable trip then currentJoiningTrip is not being used,
    // but if we are joining the trip we use this object to get proper data about the trip
    const currentTripReq = (currentJoiningTrip
      ? {
        author: selfStore.selfEmployee,
        passenger: passenger ?? selfStore.selfEmployee,
        transportType: reqTransportType,
        // ...(isTaxi && { taxiClass: TransportTypesConfig[taxiClassNormalized]?.taxiClass as TaxiClassEnum }),
        ...(joiningTaxiClass && { taxiClass: joiningTaxiClass }),
        passengerCount: passengerCount || 1,
        tariffId: currentJoiningTrip.isPersonal
          ? getTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
          : getTariffByTaxiClass(tariffsInfo, joiningTaxiClass || taxiClassNormalized),
        desiredDate: date.valueOf(),
        expected: {
          ...geo.calculatedRoute,
          cost: getPriceByTaxiClass(tariffsInfo, taxiClass || TransportTypeEnum.PERSONAL),
          outcomeCost: currentJoiningTrip?.isPersonal
            ? getOutcomeCostByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
            : getOutcomeCostByTaxiClass(tariffsInfo, joiningTaxiClass || taxiClassNormalized),
        },
        purpose: tripPurposeList?.getById(purpose),
        approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
        coopTrip,
        commentForDriver: commentary,
        timeZone,
        source: 'WEB',
        minTariffTaxi: getMinCostTaxi(tripStore.classCosts),
        commentForPurpose: commentForPurpose,
        outcomeTariffId: currentJoiningTrip?.isPersonal
          ? getOutcomeTariffByTaxiClass(tariffsInfo, TransportTypeEnum.PERSONAL)
          : getOutcomeTariffByTaxiClass(tariffsInfo, joiningTaxiClass || taxiClassNormalized),
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
    coopTripId: string | undefined,
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

    if (
      geo.calculatedRoute
      // limitsStore.currentLimit &&
      && coopTrip
    ) {
      if (isTripSearching) {
        searchTripsForBoth(
          currentTripReq,
          tariffsInfo,
          TransportTypesConfig[taxiClassNormalized]?.taxiClass as TaxiClassEnum
        );
      } else {
        const taxiClass = (data.taxiClass || '').split('-')[0] as TaxiClassEnum;

        joinCoopTrip(coopTripId ?? '', {
          ...currentTripReq,
          requestPrice: getPriceByTaxiClass(tariffsInfo, taxiClass || TransportTypeEnum.PERSONAL),
        });
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
