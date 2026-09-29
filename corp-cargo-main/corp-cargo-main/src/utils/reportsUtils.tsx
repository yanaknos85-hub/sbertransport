/* eslint-disable @typescript-eslint/no-explicit-any */
import { Descriptions } from 'antd';
import { RangeNumber, SearchedTaxiCoopTrips } from 'api/register-search';
import { TripStatus } from 'api/travel-status';
import { RUBLE_SIGN, TripRequestStatuses } from 'constants/constants.app';
import { ApprovalLabel, RegistrationCertificateLabel } from 'modules/PersonalRegistry/types/types';
import {
  FinishStatusTripFields,
  ItinerantType,
  ItinerantTypeDescriptions,
  RangeObjectEnum
} from 'modules/TaxiRegistry/types/types';
import { TripFinishedStatuses } from 'modules/OrderExecution/types/DetailedViewStatuses';
import moment from 'moment';
import * as R from 'ramda';
import React from 'react';
import { MAX_INT } from 'shared/fieldValidationRules';
import taxiTripDetailStyles from 'shared/styles/reportsDetailedView.module.scss';
import { Department } from 'stores/Corporate/Corporate.interface';
import { Organization } from 'stores/Organizations/Organizations.interface';
import {
  CoopTrip,
  OwnerInfo,
  OwnerInfoDescription,
  PaymentPeriods,
  PersonalCar,
  TripRequestReport
} from 'stores/PersonalSearch/PersonalSearch.interface';
import {
  DetailedPublicCompensation,
  InfoPassenger,
  PublicCompensationType,
  PublicCompensationTypeDescriptions,
  PublicTransportType,
  PublicTransportTypeDescriptions,
  Purpose,
  TransportCompensation,
  TripInfoForReporting,
  WaypointsInfo
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { PersonalCarDetailed, TripResponse } from 'stores/Registry/Registry.interface';
import { coopTrip } from 'stores/RegistryTaxi/Registry.interface';
import { Kpi, TaxiWaypoint } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';

import { fullNameLastFirstPat } from 'utils/employee';
import { Passenger, TaxiOptions, TaxiOptionsDescriptions } from '../stores/Trip/Trip.interface';
import { inRange } from './between';
import { formatAddress, formatWaypoints } from './formatAddress';
import { formatDistance } from './formatDistance';
import { formatRubles } from './formatRubles';
import { getTimeString } from './formatTime';
import { UUID } from './io-ts';
import uuid from './uuid';

// Для индивидуальных и совместных поездок
/** Получить статус поездки */
export const getTripStatus = (statuses: TripStatus[], status: string | undefined, emptyValue = '-'): string => status ? statuses.find(s => s.name === status)?.rusName ?? emptyValue : emptyValue;

/** Получить тип поездки */
export const getCoopTripName = (isCoop: boolean | null | undefined): string => (
  coopTrip[coopTrip.findIndex(v => Boolean(v.isCoop) === isCoop)].name
);

/** Проверить является ли один из адресов адресом отправления/назначения ВСП/ГОСБ/ТБ для индивидуальной поездки */
export const getIndividualTripVspGosbTbExist = (points: WaypointsInfo[] | TaxiWaypoint[] | null | undefined): string => points && points.some((el: WaypointsInfo | TaxiWaypoint) => el.existInVspGosbTbRegistry === true) ? 'Да' : 'Нет';

/** Получить дополнительные опции по поездке на такси */
export const getRequestOptions = (options: TaxiOptions[] | undefined, emptyValue: string): string => options && options.length ? options.map(option => TaxiOptionsDescriptions[option]).join(', ') : emptyValue;

export const transformCoopTrip = (value?: number | boolean | CoopTrip): boolean | undefined => {
  if (typeof value === 'number' || typeof value === 'boolean') {
    return Boolean(value);
  }
  if (value !== undefined) {
    return Boolean(value.value);
  }
  return undefined;
};

/** Получить разъездной характер работы для одного или всех пассажиров (при совместной поездке) */
export const getItinerantTypes = (
  itinerantType: ItinerantType | null | undefined,
  passengers?: Passenger[] | InfoPassenger[] | null
): string => {
  if (passengers && passengers.length >= 2) {
    return (passengers as any)
      .map((p: Passenger | InfoPassenger) => p.itinerantType === null ? 'нет' : ItinerantTypeDescriptions[p.itinerantType as ItinerantType] ?? '-'
      )
      .join(';\n');
  }
  return itinerantType === null ? 'нет' : ItinerantTypeDescriptions[itinerantType as ItinerantType] ?? '-';
};

export const checkPersonalShownLabel = (
  trip: TripResponse,
  label: string | JSX.Element,
  description: string | string[] | number | number[] | null | undefined
): JSX.Element | null | undefined => {
  const defaultDescription = () => (
    <Descriptions.Item
      key={uuid()}
      label={label}
      className={taxiTripDetailStyles.label}
    >
      <span className={taxiTripDetailStyles.coopTripItem}>{description}</span>
    </Descriptions.Item>
  );

  // @ts-ignore
  if (label in ApprovalLabel) {
    return trip.approvalState === TripRequestStatuses.APPROVED ? defaultDescription() : null;
  }
  // @ts-ignore
  if (label in RegistrationCertificateLabel) {
    return trip.personalCar?.ownerInfo === OwnerInfo.SPOUSE ? defaultDescription() : null;
  }
  return trip.coopTrip ? undefined : defaultDescription();
};

export const checkTaxiShownLabel = (
  trip: TripResponse,
  label: string | JSX.Element,
  description: string | string[] | number | number[] | null | undefined
): JSX.Element | null | undefined => {
  const defaultDescription = () => (
    <Descriptions.Item
      key={uuid()}
      label={label}
      className={taxiTripDetailStyles.label}
    >
      <span className={taxiTripDetailStyles.coopTripItem}>{description}</span>
    </Descriptions.Item>
  );

  // @ts-ignore
  if (label in FinishStatusTripFields) {
    return trip.status && TripFinishedStatuses.includes(trip.status) ? defaultDescription() : null;
  }
  return trip.coopTrip ? undefined : defaultDescription();
};

// Для индивидуальных поездок
/** Получить ФИО согласующего */
export const getIndividualTripApprovedBy = (
  approvedBy: Passenger | InfoPassenger | null | undefined,
  currentTripApprovedBy?: Passenger | undefined
): string => (approvedBy ? fullNameLastFirstPat(approvedBy) : fullNameLastFirstPat(currentTripApprovedBy));

/** Получить название корп. клиента для пассажира */
export const getIndividualTripContractorName = (
  organizations: Organization[],
  passenger: Passenger | null | undefined,
  emptyValue = '-'
): string => organizations.find(org => org.id === passenger?.organizationId)?.officialName ?? emptyValue;

/** Получить название подразделения для пассажира */
export const getIndividualTripDepartment = (passenger: Passenger | null | undefined, emptyValue = '-'): string => passenger && passenger.departmentName ? passenger.departmentName : emptyValue;

/** Получить адреса поездки для индивидуальной поездки. */
export const getIndividualTripAddresses = (
  points: TaxiWaypoint[] | null | undefined,
  emptyValue = '-'
): Record<string, string> => {
  const departureAddress = points ? formatAddress(points[0]) : emptyValue;
  const destinationAddress = points ? formatAddress(points[points.length - 1]) : emptyValue;
  const intermediateAddress = formatWaypoints(points);
  return {
    departureAddress, destinationAddress, intermediateAddress,
  };
};

/** Получить время ожидания по адресу для индивидуальной поездки. */
export const getIndividualTripAddressWaitTime = (points: TaxiWaypoint[] | null | undefined): Record<string, string> => {
  const departureWaitTime = getTimeString(points && points[0].waitTime);
  const destinationWaitTime = getTimeString(points && points[points.length - 1].waitTime);
  return { departureWaitTime, destinationWaitTime };
};

/** Получить ID оргструктуры для пассажира */
export const getIndividualTripOrganizationId = (passenger: Passenger | null | undefined, emptyValue = '-'): string => passenger && passenger.organizationId ? passenger.organizationId : emptyValue;

/** Получить табельный номер пассажира */
export const getIndividualTripPersonnelNumber = (passenger: Passenger | null | undefined, emptyValue = '-'): string => passenger && passenger.personnelNumber ? passenger.personnelNumber : emptyValue;

/** Получить цель поездки для пассажира */
export const getIndividualTripPurpose = (purpose: Purpose | undefined, emptyValue = '-'): string => purpose ? purpose.label : emptyValue;

/** Получить информацию о владельце ЛТ */
export const getOwnerShip = (car: PersonalCar | PersonalCarDetailed | null | undefined, emptyValue = '-'): string => {
  if (!car) {
    return emptyValue;
  }
  return car.ownerInfo && car.ownerInfo in OwnerInfo ? OwnerInfoDescription[car.ownerInfo as OwnerInfo] : emptyValue;
};

/** Получить номер свидетельства о браке */
export const getMarriageCertificateNumber = (
  car: PersonalCar | PersonalCarDetailed | null | undefined,
  passenger: Passenger | null | undefined,
  emptyValue = '-'
): string | number => car && car.ownerInfo === OwnerInfo.SPOUSE && passenger && passenger.marriageCertificateNumber
  ? passenger.marriageCertificateNumber
  : emptyValue;

/** Получить Регистрационный номер автомобиля */
export const getCarInfo = (
  car: PersonalCar | PersonalCarDetailed | null | undefined,
  emptyValue = '-'
): Record<string, string | number> => ({
  ownerShip:
    car && car.ownerInfo && car.ownerInfo in OwnerInfo ? OwnerInfoDescription[car.ownerInfo as OwnerInfo] : emptyValue,
  registrationNumber: car && car.registrationNumber ? car.registrationNumber : emptyValue,
  brand: car && car.brandName ? car.brandName : emptyValue,
  engineVolume: car && car.engineVolume ? car.engineVolume : emptyValue,
  insuranceNumber: car && car.insuranceNumber ? car.insuranceNumber : emptyValue,
});

/** Получить вид компенсации для поездок на ОТ */
export const getCompensationType = (
  compensations?: DetailedPublicCompensation[] | TransportCompensation[] | null | undefined,
  emptyValue = '-'
): string => {
  if (!compensations || !compensations.length) {
    return emptyValue;
  }

  return (compensations as any)
    .map((c: DetailedPublicCompensation | TransportCompensation) => {
      if (typeof c.compensationType === 'object' && c.compensationType !== null) {
        return c.compensationType.rusName
          ? c.compensationType.rusName
          : c.compensationType.name
            ? PublicCompensationTypeDescriptions[c.compensationType.name]
            : '';
      }
      if (c.compensationType && c.compensationType in PublicCompensationType) {
        return PublicCompensationTypeDescriptions[c.compensationType as PublicCompensationType];
      }
      return emptyValue;
    })
    .join('; ');
};

/** Получить тип общественного транспорта */
export const getPublicTransportType = (compensations: TransportCompensation[] | null | undefined): string => {
  if (!compensations) {
    return '-';
  }
  return compensations
    .map(c => (c.transportType ? PublicTransportTypeDescriptions[c.transportType as PublicTransportType] : ''))
    .join(`\n`);
};

/** Получить ожидаемую (расчётную) стоимость поездки (для поездок на ЛТ) */
// eslint-disable-next-line @stylistic/max-len
export const getExpectedCost = (expectedCost: number | null | undefined, compensations?: TransportCompensation[] | null): string => {
  if (!compensations && !expectedCost) {
    return '-';
  }
  return compensations
    ? formatRubles(compensations.reduce((sum, c) => sum + (c.ticketsCost ?? 0), 0))
    : formatRubles(expectedCost);
};

/** Получить стоимость поездки по видам компенсации (для поездок на ОТ) */
export const getCompensationTypeCost = (
  compensations: TransportCompensation[] | null | undefined,
  emptyValue = '-'
): string => {
  if (!compensations || !compensations.length) {
    return emptyValue;
  }

  return compensations
    .map(compensation => {
      if (compensation.ticketsCost) {
        return compensation.ticketsCount
          ? formatRubles(compensation.ticketsCost * compensation.ticketsCount)
          : formatRubles(compensation.ticketsCost);
      }
      return emptyValue;
    })
    .join('\n');
};

/** Получить общую стоимость поездки по видам компенсации (для поездок на ОТ) */
export const getSumCompensationTypeCost = (
  compensations: TransportCompensation[] | null | undefined,
  emptyValue = 0
): string => {
  if (!compensations || !compensations.length) {
    return '';
  }

  const sum = compensations
    .map(compensation => {
      if (compensation.ticketsCost) {
        return compensation.ticketsCount
          ? compensation.ticketsCost * compensation.ticketsCount
          : compensation.ticketsCost;
      }
      return emptyValue;
    })
    .reduce((prevItem, nextItem) => prevItem + nextItem);

  return formatRubles(sum);
};

export const getDetailedCost = (
  compensations: DetailedPublicCompensation[] | null | undefined,
  emptyValue = 0
): string => {
  if (!compensations || !compensations.length) {
    return formatRubles(emptyValue);
  }

  const costSum = compensations
    .map(compensation => {
      if (compensation.ticketsCost) {
        return compensation.ticketsCount
          ? compensation.ticketsCost * compensation.ticketsCount
          : compensation.ticketsCost;
      }
      return emptyValue;
    })
    .reduce((prevItem, nextItem) => prevItem + nextItem);

  return formatRubles(costSum);
};

/** Получить ожидаемое расстояние поездки (для поездок на ЛТ) */
export const getExpectedDistance = (distance: number): string => `${formatDistance(distance)} км.`;

// Для совместных поездок
export const getCoopTripCompanionFIO = (
  passengersInfo: Record<string, string | number>[],
  param: string
): JSX.Element[] => passengersInfo.map((item, index) => (
  <div>
    <div className={taxiTripDetailStyles.companionContainer}>{index ? `Попутчик №${index}` : 'Инициатор'}</div>
    <div>{item[param]}</div>
  </div>
));

export const getCoopTripParam = (
  passengersInfo: Record<string, string | number>[],
  param: string
): (string | number)[] => passengersInfo.map(item => item[param]);

/** Получить экономию для текущего заказчика, в совместной поездке. */
export const getCoopTripCurrentSaving = (coopKpi: Kpi | undefined, currentTripId: string, emptyValue = '-'): string => {
  if (!coopKpi?.ordersKpi?.length) {
    return emptyValue;
  }

  const currentSaving = coopKpi.ordersKpi.find(item => item.orderId === currentTripId);
  return formatRubles(currentSaving && currentSaving.savings);
};

/** Получить ID оргструктуры для пассажира совместной поездки */
export const getCoopTripOrganizationId = (
  passengers: Passenger[] | undefined,
  passenger: Passenger | null | undefined,
  emptyValue = '-'
): string => {
  if (!passengers || !passenger) {
    return emptyValue;
  }

  const currentPassenger = passengers.find(item => item.id === passenger.id);

  return currentPassenger?.organizationId || emptyValue;
};

/** Получить название корп. клиента для пассажира совместной поездки */
export const getCoopTripContractorName = (
  organizations: Organization[],
  passengers: Passenger[] | undefined,
  passenger: Passenger | null | undefined,
  emptyValue = '-'
): string => {
  if (!passengers || !passenger) {
    return emptyValue;
  }

  const currentPassenger = passengers.find(item => item.id === passenger.id);

  return organizations.find(org => org.id === currentPassenger?.organizationId)?.officialName ?? emptyValue;
};

export const getCurrentSharedRideIdArray = (trips: TripInfoForReporting[]): string[] => {
  const coopTripsWithPassengers = trips.filter(trip => trip.coopTrip && trip.passengers && trip.passengers.length > 1);
  const coopTripsIdMap = new Map();
  coopTripsWithPassengers.forEach(item => coopTripsIdMap.set(item.sharedRideId, item));
  const uniqSharedRideIdArray = [] as string[];
  coopTripsIdMap.forEach(
    (item: TripInfoForReporting) => item.sharedRideId && uniqSharedRideIdArray.push(item.sharedRideId)
  );
  return uniqSharedRideIdArray;
};

export const getCoopTrips = (
  coopTripsResponse: SearchedTaxiCoopTrips,
  sharedRideId: string | null | undefined,
  emptyValue = undefined
): TripInfoForReporting[] | undefined => (
  sharedRideId ? coopTripsResponse.find(item => item.sharedRideId === sharedRideId)?.trips ?? emptyValue : emptyValue
);

export const getTripParam = (trips: TripInfoForReporting[], emptyValue = '-'): Record<string, string> => {
  const params = {
    costCenter: [],
    fio: [],
    cost: [],
    distance: [],
    expectedСost: [],
  } as Record<string, any>;

  const coopTrips = new Map();
  trips.forEach(trip => coopTrips.set(trip.passenger.id, trip));

  const sortedUniqTrips = Array.from(coopTrips, ([, value]) => value).sort(
    (prev, next) => prev.creationTime - next.creationTime
  );

  sortedUniqTrips.forEach((trip: TripInfoForReporting) => {
    params.costCenter.push(trip.costCenter || emptyValue);
    params.fio.push(fullNameLastFirstPat(trip.passenger));
    params.cost.push(formatRubles(trip.factData?.tripFactPrice));
    params.distance.push(formatDistance(trip.expected.distance));
    params.expectedСost.push(formatRubles(trip.expected?.cost).replace(RUBLE_SIGN, ''));
  });

  for (const key in params) {
    const paramsForString = params[key].map((item: string) => (item === emptyValue ? item : `${item};`));
    params[key] = paramsForString.join('\n');
  }

  return params;
};

export const getTripParams = (
  trip: TripInfoForReporting,
  trips: TripInfoForReporting[] | undefined,
  emptyValue = '-'
): Record<string, string> => {
  const isCoop = trip.coopTrip && trip.passengers && trip.passengers.length > 1;
  const params = trips ? getTripParam(trips) : {};

  const costCenter = isCoop ? params.costCenter : trip.costCenter || emptyValue;
  const fio = isCoop ? params.fio : fullNameLastFirstPat(trip.passenger);
  const cost = isCoop ? params.cost : formatRubles(trip.factData?.tripFactPrice).replace(RUBLE_SIGN, '');
  const distance = isCoop ? params.distance : formatDistance(trip.expected?.distance);
  const expectedСost = isCoop ? params.expectedСost : formatRubles(trip.expected?.cost).replace(RUBLE_SIGN, '');

  return {
    costCenter,
    fio,
    cost,
    distance,
    expectedСost,
  };
};

export const getGeneralPrice = (trips: TripRequestReport[] | undefined, emptyValue = '-'): string => {
  if (!trips || !trips.length) {
    return emptyValue;
  }
  if (trips.length === 1) {
    return formatRubles(trips[0].expected.cost);
  }
  const coopTrips = new Map();
  trips.forEach(trip => coopTrips.set(trip.passenger?.id, trip));
  const uniqTrips = Array.from(coopTrips, ([, value]) => value);

  return formatRubles(
    uniqTrips.reduce((sum, trip) => {
      if (trip.kpi) {
        return sum + trip.kpi.totalCost;
      }
      return sum;
    }, 0)
  );
};

/**
 * Возвращает определенное значение (Период оплаты) в зависимости от даты поездки для журнала и детального просмотра
 * @param desiredDate - ожидаемая дата поездки
 */
export const getPaidPeriodValueByTripDateTime = (
  desiredDate: number | undefined | null
): 'I' | 'II' | 'III' | 'IV' | '-' => {
  if (!desiredDate) {
    return '-';
  }
  const currentDay = moment(desiredDate).date();
  if (inRange(1, 7)(currentDay)) {
    return 'I';
  }
  if (inRange(8, 15)(currentDay)) {
    return 'II';
  }
  if (inRange(16, 23)(currentDay)) {
    return 'III';
  }
  return 'IV';
};

/**
 * Возвращает определенное значение (Период оплаты) в зависимости от параметра - период оплаты для журнала и детального просмотра
 * @param period - значение периода оплаты, приходящее от бэекенда
 */
export const getPaidPeriodValueByPaymentQuarter = (period: number | null | undefined): string => {
  const periods = {
    1: 'I', 2: 'II', 3: 'III', 4: 'IV',
  };
  return period ? periods[period as PaymentPeriods] : '-';
};

/**
 * Обрабатывает данные для отображения подразделений в журнале
 * @param departments
 * @param depId
 */

export const getFullPathDepartments = (
  departments: Record<string, Department | undefined>,
  depId: string
): string[] => {
  const getFullPath = (id: string, acc = [id]): string[] => {
    const dep = departments[id];
    if (!dep) {
      return [];
    }

    const { parent } = dep;

    return parent?.id ? getFullPath(parent?.id, [...acc, parent?.id]) : [...acc].reverse();
  };
  return getFullPath(depId);
};

export const getDepartmentsTree = (
  id: string,
  dep: Record<string, Department | undefined>
): {
  passengerDepartmentOneVisible: string;
  passengerDepartmentTwoVisible: string;
  passengerDepartmentThreeVisible: string;
  passengerDepartmentFourVisible: string;
  passengerDepartmentFiveVisible: string;
  passengerDepartmentSixVisible: string;
} => {
  const fullPath = getFullPathDepartments(dep, id).map(i => dep[i]?.departmentName);
  return {
    passengerDepartmentOneVisible: fullPath[0] || '-',
    passengerDepartmentTwoVisible: fullPath[1] || '-',
    passengerDepartmentThreeVisible: fullPath[2] || '-',
    passengerDepartmentFourVisible: fullPath[3] || '-',
    passengerDepartmentFiveVisible: fullPath[4] || '-',
    passengerDepartmentSixVisible: fullPath[5] || '-',
  };
};

export const getLast = (departmentId: UUID[][]): UUID[] | undefined => (
  R.findLast<UUID[]>(R.complement(R.isEmpty))(departmentId)
);

/** обрабатывает данные полученные из фильтров с возможностью выбора одного значения или диапозона */
export const transformToRangeObject = (
  value: number | RangeNumber | undefined,
  type?: RangeObjectEnum
): RangeNumber | undefined => {
  if (typeof value === 'number') {
    return { start: value, end: value };
  }

  if (type === RangeObjectEnum.cost) {
    return value ? { start: value.start ?? 0, end: value.end ?? MAX_INT / 100 } : undefined;
  }

  if (type === RangeObjectEnum.time) {
    return value ? { start: value.start ?? 0, end: value.end ?? MAX_INT / 60000 } : undefined;
  }

  return value ? { start: value.start ?? 0, end: value.end ?? MAX_INT } : undefined;
};

export const clearButtonActive = (values: Record<string, any>): boolean => (
  Object.entries(values).some(([key, value]: [string, any]) => {
    if (key === 'creationDate' || key === 'approveDate' || key === 'desiredDate' || key === 'actualDepartureDate') {
      // Проверка для дат: value может быть объектом {mode, value} или null/undefined
      if (value && typeof value === 'object' && 'value' in value) {
        return Array.isArray(value.value) && value.value.some((item: unknown) => item !== null);
      }
      return false;
    }
    if (key === 'employeeDepartmentSet') {
      return value.some((item: [] | string) => typeof item === 'string' || item.some(el => el !== null));
    }
    if (typeof value === 'object' && value !== null) {
      return Object.values(value).some(el => el !== null);
    }
    return value !== undefined && value !== '' && value !== null;
  })
);
