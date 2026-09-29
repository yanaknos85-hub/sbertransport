import { convertToRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { useGetLimitByRequestId } from 'api/limits';
import {
  getCarInfo,
  getCoopTripName,
  getIndividualTripApprovedBy,
  getMarriageCertificateNumber,
  getPaidPeriodValueByTripDateTime,
  getTripStatus,
  getIndividualTripAddresses
} from 'utils/reportsUtils';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { formatTime } from 'utils/formatTime';
import { fullNameLastFirstPat } from 'utils/employee';
import { PersonalSearchResponse } from 'stores/PersonalSearch/PersonalSearch.interface';
import { Records } from '../../TaxiRegistry/types/types';
import { DATE_FORMAT } from 'constants/constants.app';

export const useIndividualTripDescription = (
  trip: TripResponse,
  tripReport: PersonalSearchResponse,
  labels: Record<string, string>,
  order: TripResponse
): {
  generalInfo: Records;
  userInfo: Records;
  tripInfo: Records;
  sctructureInfo: Records;
  planFactInfo: Records;
  additionalInfo: Records;
} => {
  const { data: tripStatus } = useGettingAllTravelStatuses();
  const { data: limit } = useGetLimitByRequestId(trip.id);

  const {
    ownerShip, registrationNumber, brand, engineVolume, insuranceNumber,
  } = getCarInfo(trip.personalCar);

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    trip.expected.waypoints
  );

  const passenger = tripReport.content[0];
  const passengersInfo = [];

  if (trip.coopTrip && passenger.joinedPassengers) {
    const joinedPassengers = passenger.joinedPassengers.split(', ');
    for (let i = 0; i < joinedPassengers.length; i++) {
      const [name, number] = joinedPassengers[i].split(' (');
      // @ts-ignore
      passengersInfo.push([`ФИО пассажира ${i + 1}`, name]);
      // @ts-ignore
      passengersInfo.push([`Табельный номер пассажира ${i + 1}`, number.slice(0, -1)]);
    }
  }

  const finalReportTrip = trip.coopTrip ? passenger : tripReport.content[0];

  return {
    generalInfo: [
      ['Наименование организации ТБ', '-'],
      [labels.requestIdVisible, trip.humanReadableId],
      [labels.costCenter, finalReportTrip.costCenter ?? '-'],
      [labels.requestStatus, getTripStatus(tripStatus, trip.status)],
      ['Код подразделения', finalReportTrip?.department?.code],
      ['ID поездки', trip.humanReadableId ?? '-'],
      ['Тип поездки', getCoopTripName(trip.coopTrip)],
    ] as Records,

    userInfo: [
      ['Табельный номер заявителя', finalReportTrip?.author?.personnelNumber],
      ['ФИО заявителя', fullNameLastFirstPat(finalReportTrip.author)],
      ['Количество присоединенных заявок', finalReportTrip?.totalSharedRequestCount ?? '-'],
      ['Статус пользователя', finalReportTrip?.personalCar ? 'Водитель' : 'Пассажир'],
      ['Количество забронированных мест (без учета водителя)', finalReportTrip.passengerCount || '-'],
      ['Цель поездки', trip.purpose?.label ?? '-'],
      ['ID лимита', limit?.humanReadableId ?? '-'],
      ['ID тарифа', finalReportTrip.tariff ?? '-'],
      ['Комментарий для водителя', '-'],
      ['Объем двигателя автомобиля', engineVolume],
      [labels.brandName, brand],
      [labels.registrationCertificate, getMarriageCertificateNumber(trip.personalCar, finalReportTrip.passenger)],
      [labels.registrationNumber, registrationNumber],
      [labels.insuranceNumber, insuranceNumber],
      [labels.ownership, ownerShip],
    ] as Records,

    tripInfo: [
      [labels.desiredDateRange, formatTime(finalReportTrip.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Дата утверждение заявки', formatTime(finalReportTrip.approveDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Адрес отправления', departureAddress],
      ['Промежуточные адреса', intermediateAddress],
      ['Количество точек в маршруте', trip.expected.waypoints.length],
      [labels.waypointToVisible, destinationAddress],
      ['Дата и время завершение поездки', '-'],
      ['Дата закрытия заявки', '-'],
      ['Контрольный срок заявки', '-'],
      ['Нарушение КС', finalReportTrip.deadlineViolation],
      ['Кол-во пунктов маршрута поездки с авточек-ин', finalReportTrip.expected.waypointsCountWithCheckIn],
      ['Кол-во пунктов поездки с ручным чек-ин', finalReportTrip.expected.waypointsCountWithoutCheckIn],
      [labels.relatedApplication, order && trip.payRequestIds?.length ? order.humanReadableId : '-'],
    ] as Records,

    sctructureInfo: [
      ['Код подразделения. OE', finalReportTrip?.department?.code],
      ['Подразделение 1 уровня', finalReportTrip.passengerDepartment1 ?? '-'],
      ['Подразделение 2 уровня', finalReportTrip.passengerDepartment2 ?? '-'],
      ['Подразделение 3 уровня', finalReportTrip.passengerDepartment3 ?? '-'],
      ['Подразделение 4 уровня', finalReportTrip.passengerDepartment4 ?? '-'],
      ['Подразделение 5 уровня', finalReportTrip.passengerDepartment5 ?? '-'],
      ['Подразделение 6 уровня', finalReportTrip.passengerDepartment6 ?? '-'],
    ] as Records,

    planFactInfo: [
      ['Плановая стоимость, руб', convertToRubles(trip.expected.cost)],
      ['Плановое расстояние, км', formatDistance(trip.expected.distance)],
      ['Фактическая стоимость, руб', finalReportTrip.factData?.tripFactPrice ? convertToRubles(finalReportTrip.factData?.tripFactPrice) : '-'],
      ['Фактическое расстояние, км', formatDistance(finalReportTrip?.factData?.tripFactDistance)],
      ['Сумма к выплате, руб', finalReportTrip.factData?.tripFactPrice ? convertToRubles(finalReportTrip.factData?.tripFactPrice * 0.87) : '-'],
      ['Сумма к выплате по коду 4661', '-'],
      ['Сумма к выплате по коду 4664', '-'],
      ['Сумма к выплате по коду 4665', '-'],
      [labels.paidPeriod, getPaidPeriodValueByTripDateTime(trip.desiredDate)],
    ] as Records,

    additionalInfo: [
      ...passengersInfo,
      ['ФИО согласующего заявку', getIndividualTripApprovedBy(trip.approvedBy)],
      ['Табельный номер согласующего заявку', trip.approvedBy?.personnelNumber],
      ['Оценка поездки пользователем', trip.requestRating?.rating ?? 'Отсутствует'],
      ['Комментарий пользователя к оценке', trip.requestRating?.ratingComment ?? 'Отсутствует'],
      ['ID Совместной поездки', trip.sharedRideId || '-'],
    ] as Records,
  };
};
