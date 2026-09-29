import { convertToRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { TripResponse } from 'stores/Registry/Registry.interface';
import {
  getTripStatus,
  getIndividualTripAddresses,
  getIndividualTripContractorName
} from 'utils/reportsUtils';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { formatTime } from 'utils/formatTime';
import { fullNameLastFirstPat } from 'utils/employee';
import { CarSharingSearchResponse } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { Records } from '../../TaxiRegistry/types/types';
import { DATE_FORMAT } from 'constants/constants.app';
import { useOrganizations } from 'api/organizations';

export const useIndividualTripDescription = (
  trip: TripResponse,
  tripReport: CarSharingSearchResponse
): {
  generalInfo: Records;
  userInfo: Records;
  tripInfo: Records;
  sctructureInfo: Records;
  planFactInfo: Records;
  additionalInfo: Records;
  estimation: Records;
} => {
  const { data: tripStatus } = useGettingAllTravelStatuses();
  const { content: organizations } = useOrganizations().data.organizationResponse;

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    trip.expected.waypoints
  );

  const finalReportTrip = tripReport.content[0];

  return {
    generalInfo: [
      ['Номер заявки', trip.humanReadableId ?? '-'],
      ['Статус заявки', getTripStatus(tripStatus, trip.status)],
      ['Организация', finalReportTrip.organization ?? '-'],
      ['Дата и время создания заявки', formatTime(trip.creationTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['МВЗ', trip.passenger.mvz ?? '-'],
      ['Контрагент', getIndividualTripContractorName(organizations, trip.passenger)],
      ['Номер id аренды во внешней системе', trip.trip?.rentId ?? '-'],
    ] as Records,

    userInfo: [
      ['ФИО сотрудника', fullNameLastFirstPat(trip.author)],
      ['Табельный номер сотрудника', trip?.author?.personnelNumber],
      ['Должность', trip?.author?.positionName],
      ['Телефон', trip?.author?.mobilePhone],
      ['Подразделение сотрудника', trip?.author?.departmentName],
    ] as Records,

    tripInfo: [
      ['Желаемая дата и время поездки', formatTime(trip.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Дата и время начала аренды', formatTime(trip.trip?.rentCreatedAt, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Дата и время завершения аренды', formatTime(trip.trip?.rentFinishedAt, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Плановое время брони/аренды', formatTime(trip.expected.time, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Фактическое время брони', formatTime(trip.trip?.reserveTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Плановое время поездки', formatTime(trip.trip?.drivingTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Фактическое время ожидания', formatTime(trip.trip?.parkingTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Плановый пробег, км', formatDistance(trip.expected.distance) ?? '-'],
      ['Фактический пробег, км', formatDistance(trip.trip?.drivingLength) ?? '-'],
    ] as Records,

    sctructureInfo: [
      ['Планируемый адрес отправления', departureAddress],
      ['Фактический адрес отправления', trip.trip?.startAddress ?? '-'],
      ['Планируемый промежуточный адрес', intermediateAddress],
      ['Фактический промежуточный адрес', '-'],
      ['Планируемый адрес прибытия', destinationAddress],
      ['Фактический адрес прибытия', trip.trip?.finishAddress ?? '-'],
    ] as Records,

    planFactInfo: [
      ['Плановая стоимость брони, руб', trip.trip?.reserveTimeCost ? convertToRubles(trip.trip?.reserveTimeCost) : '-'],
      ['Фактическая стоимость поездки, руб', trip.trip?.drivingTimeCost ? convertToRubles(trip.trip?.drivingTimeCost) : '-'],
      ['Фактическая стоимость ожидания, руб', trip.trip?.parkingTimeCost ? convertToRubles(trip.trip?.parkingTimeCost) : '-'],
      ['Дополнительная стоимость за пробег, руб', trip.trip?.drivingLengthCost ? convertToRubles(trip.trip?.drivingLengthCost) : '-'],
      ['Итоговая плановая стоимость аренды, руб', convertToRubles(trip.expected.cost) ?? '-'],
      ['Итоговая фактическая стоимость аренды, руб', trip.trip?.totalCost ? convertToRubles(trip.trip?.totalCost) : '-'],
    ] as Records,

    additionalInfo: [
      ['Модель', trip.trip?.carModel ?? '-'],
      ['Гос.номер', trip.trip?.carNumber ?? '-'],
      ['Количество забронированых мест в заявке', trip.passengerCount ?? '-'],
    ] as Records,

    estimation: [
      ['Оценка', trip.requestRating?.rating ?? 'Отсутствует'],
    ] as Records,
  };
};
