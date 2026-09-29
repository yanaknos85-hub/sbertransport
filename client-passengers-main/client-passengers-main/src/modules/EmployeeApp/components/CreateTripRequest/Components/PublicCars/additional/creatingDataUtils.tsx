/* eslint-disable @typescript-eslint/no-explicit-any */
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd';
import { Moment } from 'moment';

import { RouteModel } from 'shared/models/geo/Route.model';
import { IGeoStore } from 'stores/Geo/Geo.interface';

import { PublicTransportTypeEnum, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  CityTripCompensation,
  InnerCityTransportRequestInfo,
  MinTariffTaxi,
  PublicTripCompensation,
  SavedFileInfo,
  TTariffPublic,
  TransportCompensation,
  TripData,
  TripPurpose
} from 'stores/Trip/Trip.interface';
import { fromRubles } from 'utils';

const getExpectedValue = (geo: IGeoStore): RouteModel | undefined => geo.calculatedRoute;

export const getSavingData = (
  formPassenger: EmployeeModel,
  author: EmployeeModel,
  purpose: TripPurpose,
  cost: number,
  date: Moment,
  geo: IGeoStore,
  transportCompensation: TransportCompensation[],
  savedFileData?: SavedFileInfo[],
  tariffId?: string,
  payRequestId?: string,
  minTariffTaxi?: MinTariffTaxi,
  commentForPurpose?: string
): PublicTripCompensation => ({
  author,
  passenger: formPassenger,
  transportType: TransportTypeEnum.PUBLIC,
  desiredDate: date.valueOf(),
  expected: new RouteModel({ ...(getExpectedValue(geo) as RouteModel), cost: fromRubles(cost / 100) }),
  segments: geo.calculatedRoute?.segments,
  waypoints: geo.calculatedRoute?.waypoints,
  purpose,
  compensationDocuments: savedFileData,
  transportCompensation,
  tariffId,
  source: 'WEB',
  payRequestId: payRequestId,
  minTariffTaxi: minTariffTaxi,
  commentForPurpose: commentForPurpose,
});

const getQuantity = (array: any[], publicTransportType: string): number => {
  const tickets = array.filter(x => x.publicTransportType === publicTransportType);
  return tickets.reduce((total, current) => total + current.ticketCount, 0);
};

const getTripData = (childForm: FormInstance): TripData => {
  const { tripsInfo } = childForm.getFieldsValue();

  return (
    tripsInfo
      ? {
        metroTicketsQuantity: getQuantity(tripsInfo, PublicTransportTypeEnum.METRO),
        tramTicketsQuantity: getQuantity(tripsInfo, PublicTransportTypeEnum.TRAM),
        trolleybusTicketsQuantity: getQuantity(tripsInfo, PublicTransportTypeEnum.TROLLEYBUS),
        busTicketsQuantity: getQuantity(tripsInfo, PublicTransportTypeEnum.BUS),
      }
      : {}
  ) as TripData;
};

export const getCityTripSavingData = (
  data: InnerCityTransportRequestInfo,
  currentTariff: TTariffPublic | undefined,
  childForm: FormInstance
): CityTripCompensation | null => currentTariff
  ? {
    ...data,
    tariffId: currentTariff.id,
    tripData: getTripData(childForm),
    tariffData: {
      metroTicketCost: currentTariff.metroTicketCost,
      tramTicketCost: currentTariff.tramTicketCost,
      trolleybusTicketCost: currentTariff.trolleybusTicketCost,
      busTicketCost: currentTariff.busTicketCost,
      metroAvailability: currentTariff.metroAvailability,
      tramAvailability: currentTariff.tramAvailability,
      trolleybusAvailability: currentTariff.trolleybusAvailability,
      busAvailability: currentTariff.busAvailability,
    },
  }
  : null;
