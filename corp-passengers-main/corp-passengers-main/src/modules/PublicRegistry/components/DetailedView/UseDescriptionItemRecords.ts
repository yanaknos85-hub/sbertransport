import { useTranslation } from 'i18n';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { formatTime } from 'utils/formatTime';
import {
  getCompensationType,
  getPaidPeriodValueByTripDateTime,
  getTripStatus,
  getIndividualTripAddresses,
  getIndividualTripApprovedBy
} from 'utils/reportsUtils';
import { fullNameLastFirstPat } from 'utils/employee';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { Records } from '../../types/types';
import { SearchResponse } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { convertToRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { DATE_FORMAT } from 'constants/constants.app';

export const useDescriptionItemRecords = (
  request: TripResponse,
  order: TripResponse,
  tripData: SearchResponse
): {
  generalInfo: Records;
  userInfo: Records;
  tripInfo: Records;
  sctructureInfo: Records;
  planFactInfo: Records;
  additionalInfo: Records;
} => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;
  const tripStatus = useGettingAllTravelStatuses().data;

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    request.expected.waypoints
  );

  const reportTrip = tripData.content[0];

  return {
    generalInfo: [
      ['Наименование организации ТБ', '-'],
      [labels.requestIdVisible, request.humanReadableId],
      [labels.costCenter, reportTrip?.costCenter ?? '-'],
      [labels.requestStatus, getTripStatus(tripStatus, request.status)],
      ['Код подразделения', reportTrip?.department?.code],
    ] as Records,

    userInfo: [
      ['Табельный номер заявителя', reportTrip?.author?.personnelNumber],
      ['ФИО заявителя', fullNameLastFirstPat(reportTrip.author)],
      ['Цель поездки', reportTrip.purpose?.purpose ?? '-'],
      ['ID лимита', reportTrip?.humanReadableId ?? '-'],
      ['ID тарифа', reportTrip?.humanReadableLimitId ?? '-'],
    ] as Records,

    tripInfo: [
      [labels.desiredDateRange, formatTime(reportTrip.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Дата согласования заявки', formatTime(reportTrip.approveDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Вид компенсации', getCompensationType(request.transportCompensation)],
      ['Вид транспорта', '-'],
      ['Количество билетов', '-'],
      ['Адрес отправления', departureAddress],
      ['Промежуточные адреса', intermediateAddress],
      ['Количество точек в маршруте', request.expected.waypoints.length],
      [labels.waypointToVisible, destinationAddress],
      ['Дата и время завершение поездки', '-'],
      ['Дата закрытия заявки', '-'],
      ['Контрольный срок заявки', '-'],
      ['Нарушение КС', reportTrip.deadlineViolation],
      [labels.relatedApplication, order && request.payRequestIds?.length ? order.humanReadableId : '-'],
    ] as Records,

    sctructureInfo: [
      ['Код подразделения. OE', reportTrip?.department?.code],
      ['Подразделение 1 уровня', reportTrip.passengerDepartment1 ?? '-'],
      ['Подразделение 2 уровня', reportTrip.passengerDepartment2 ?? '-'],
      ['Подразделение 3 уровня', reportTrip.passengerDepartment3 ?? '-'],
      ['Подразделение 4 уровня', reportTrip.passengerDepartment4 ?? '-'],
      ['Подразделение 5 уровня', reportTrip.passengerDepartment5 ?? '-'],
      ['Подразделение 6 уровня', reportTrip.passengerDepartment6 ?? '-'],
    ] as Records,

    planFactInfo: [
      ['Стоимость, руб', convertToRubles(request.expected.cost)],
      ['Расстояние, км', formatDistance(request.expected.distance)],
      ['Сумма к выплате, руб', reportTrip.factData?.tripFactPrice ? convertToRubles(reportTrip.factData?.tripFactPrice * 0.87) : '-'],
      ['Сумма к выплате по коду 4661', '-'],
      ['Сумма к выплате по коду 4664', '-'],
      ['Сумма к выплате по коду 4665', '-'],
      [labels.paidPeriod, getPaidPeriodValueByTripDateTime(request.desiredDate)],
    ] as Records,

    additionalInfo: [
      ['ФИО согласующего заявку', getIndividualTripApprovedBy(request.approvedBy)],
      ['Табельный номер согласующего заявку', request.approvedBy?.personnelNumber],
      ['Оценка поездки пользователем', request.requestRating?.rating ?? 'Отсутствует'],
      ['Комментарий пользователя к оценке', request.requestRating?.ratingComment ?? 'Отсутствует'],
    ] as Records,
  };
};
