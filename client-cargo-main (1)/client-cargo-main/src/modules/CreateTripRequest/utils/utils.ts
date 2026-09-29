import { FormInstance } from 'antd/es/form/Form';
import moment from 'moment';

import { LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import {
  ITripTariff, TaxiClassEnum, Transport, TripPurpose, WeekdaysType
} from 'stores/Trip/Trip.interface';

import { DatePurposeValidationObject, FormValues } from '../types/types';

const getDayNumberByWeekday = (day: any): WeekdaysType => {
  switch (day) {
    case 0:
      return 'SUNDAY';
    case 1:
      return 'MONDAY';
    case 2:
      return 'TUESDAY';
    case 3:
      return 'WEDNESDAY';
    case 4:
      return 'THURSDAY';
    case 5:
      return 'FRIDAY';
    case 6:
      return 'SATURDAY';
    default:
      return 'SUNDAY';
  }
};

/**
 * Возвращает строку времени формата hh:mm:ss
 * @param date
 */
const dateToFormattedTimeStringConverter = (date: string): string => `${moment(date).hours()}:${moment(date).minutes()}:${moment(date).seconds()}`;

export const calculateTripCost = (classCosts: TripPriceModel[], transport: Transport | undefined): number | undefined => classCosts.find((x: TripPriceModel) => x.transportType.name !== TransportTypeEnum.TAXI ? x.transportType.name === transport : x.taxiClass === transport
)?.cost;

export const calculateTaxiClassCost = (
  costs: TripPriceModel[],
  transportType: string | false | undefined,
  taxiClass?: string | undefined
): any => costs.find((x: TripPriceModel) => {
  if (x.transportType.name !== 'TAXI') {
    return x.transportType.name === transportType;
  }
  return x.taxiClass === taxiClass;
});

export const calculateEconomyCost = (oldCost: number, percent: number): number => ((100 - percent) * oldCost) / 100;

/**
 * Возвращает объект, содержащий выбранную цель поездки и флаг валидности указанной даты
 * @param form - форма
 * @param purposes - все цели
 * @param currentPurposeId - цель в текущем состоянии
 * @param selectedPurposeId - цель, выбранная из списка
 */
export const validateSelectedDateByPurpose = (
  form: FormInstance,
  purposes: TripPurpose[],
  currentPurposeId: string,
  selectedPurposeId?: string
): DatePurposeValidationObject => {
  // FIXME sonarjs/cognitive-complexity
  const currentDate = form.getFieldValue('date');
  let result = true;

  const purposeId = selectedPurposeId || currentPurposeId;
  const purpose = purposeId !== '' ? purposes.find(el => el.id === purposeId) : undefined;

  if (currentDate && purpose) {
    if (purpose.tripPurposeDates) {
      purpose.tripPurposeDates.forEach(date => {
        if (
          !moment(currentDate).isBetween(
            moment(date.startDate).set({
              hour: 0, minute: 0, second: 0,
            }),
            moment(date.endDate).set({
              hour: 23, minute: 59, second: 59,
            })
          )
        ) {
          result = false;
        }
      });
    }
    if (purpose.tripPurposeTimes) {
      purpose.tripPurposeTimes.forEach(times => {
        const currentTime = dateToFormattedTimeStringConverter(currentDate);
        const startTime = times.startTime ? dateToFormattedTimeStringConverter(times.startTime) : '00';
        const endTime = times.endTime ? dateToFormattedTimeStringConverter(times.endTime) : '00';
        if (currentTime < startTime || currentTime > endTime) {
          result = false;
        }
      });
    }
    if (purpose.tripPurposeWeekdays && purpose.tripPurposeWeekdays.length !== 0) {
      const purposeWeekdays = purpose.tripPurposeWeekdays.map(weekday => weekday.weekday);
      if (!purposeWeekdays.includes(getDayNumberByWeekday(moment(currentDate).day()))) {
        result = false;
      }
    }
  }
  form.validateFields();
  return { purposeId, isValid: result };
};

/**
 * Возвращает число процентов для строки состояния остатка по лимиту для видов транспорта
 * @param limitSharing
 */
export const getAvailablePercentage = (limitSharing?: LimitSharing | null): number => {
  if (limitSharing) {
    const { balance, sum } = limitSharing.limitSharingPerPeriodDTO || limitSharing;
    return Number(((balance * 100) / sum).toFixed(1));
  }

  return 0;
};

/**
 * Возвращает объект со значениями формы с тарифом в зависимотси от выбранного вида транспорта из объекта costs
 * @param data
 * @param costs
 */
export const updateFormValuesBySelectedTransport = (data: FormValues, costs: ITripTariff[]): any => {
  const { taxiClass } = data;
  const taxiClassNormalized = taxiClass?.split('-')[0];

  const foundCost = costs.find(
    el => el.taxiClass === taxiClassNormalized || el.transportType.name === taxiClassNormalized
  );

  return foundCost ? { ...data, tariffId: foundCost.id ?? '' } : data;
};

export const getTariffByTaxiClass = (costs: ITripTariff[], taxiClass: TaxiClassEnum | TransportTypeEnum): string => {
  const foundCost = costs.find(el => el.taxiClass === taxiClass || el.transportType.name === taxiClass);
  return (foundCost && foundCost.id) || '';
};

export const getTimeZone = (): string => `GMT${moment().format('ZZ')}`.slice(0, -2);
