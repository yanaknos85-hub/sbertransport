import { formatRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { useOrganizations } from 'api/organizations';
import {
  getIndividualTripContractorName,
  getIndividualTripPersonnelNumber,
  getTripStatus
} from 'utils/reportsUtils';
import { formatTime, getTimeString } from 'utils/formatTime';
import { fullNameLastFirstPat } from 'utils/employee';
import { useGetCarShаringTripStatuses } from 'api/car-sharing-report';
import { usePosition } from 'api/positions';
import { getIndividualTripAddresses } from 'utils/reportsUtils';
import { TripRequestReport } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { Records } from '../../TaxiRegistry/types/types';

export const useTripDescription = (
  trip: TripResponse,
  tripReport: TripRequestReport,
  labels: Record<string, string>
): Records => {
  const { content: organizations } = useOrganizations().data.organizationResponse;
  const { data: tripStatus } = useGetCarShаringTripStatuses();
  const { data: position } = usePosition(trip.passenger?.organizationId, trip.passenger?.positionId);

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    trip.expected.waypoints
  );

  return [
    // Номер заявки
    [labels.tripId, trip.humanReadableId ?? '-'],
    // Желаемая дата и время поездки
    [labels.desiredDateRange, formatTime(trip.desiredDate)],
    // МВЗ
    [labels.costCenter, trip.passenger.mvz ?? '-'],
    // Статус заявки
    [labels.tripStatus, getTripStatus(tripStatus, trip.status)],
    // Контрагент
    [labels.contractorName, getIndividualTripContractorName(organizations, trip.passenger)],
    // Номер id аренды во внешней системе
    // @ts-ignore
    ['Номер id аренды во внешней системе ', trip.trip?.rentId ?? '-'],
    // Фамилия, имя, отчество сотрудника
    [labels.corpClientFio, fullNameLastFirstPat(trip.passenger)],
    // Табельный номер сотрудника
    [labels.corpClientPersonnelNumber, getIndividualTripPersonnelNumber(trip.passenger)],
    // Должность
    ['Должность ', position?.positionName ?? '-'],
    // Телефон
    ['Телефон ', trip.passenger.mobilePhone ?? '-'],
    // Время создания заявки
    [labels.creationDate, formatTime(trip.creationTime)],
    // Транспортное средство
    // @ts-ignore
    ['Транспортное средство ', trip.trip?.carModel ?? '-'],
    // Количество забронированных мест в заявке
    ['Количество забронированных мест в заявке ', trip.passengerCount ?? '-'],
    // Количество пассажиров (без учета водителя)
    ['Количество пассажиров (без учета водителя) ', (trip.passengerCount || 2) - 1],
    // Адрес отправления
    ['Адрес отправления, планируемый ', departureAddress],
    // Адрес отправления, по факту начала аренды
    // @ts-ignore
    ['Адрес отправления, по факту начала аренды ', trip.trip?.startAddress ?? '-'],
    // Промежуточные адреса маршрута
    ['Промежуточные адреса маршрута ', intermediateAddress],
    // Адрес прибытия, планируемый
    ['Адрес прибытия, планируемый ', destinationAddress],
    // Адрес прибытия, по факту окончания аренды
    // @ts-ignore
    ['Адрес прибытия, по факту окончания аренды ', trip.trip?.finishAddress ?? '-'],
    // Дата, время начала аренды
    // @ts-ignore
    ['Дата, время начала аренды ', formatTime(trip.trip?.rentCreatedAt) ?? '-'],
    // Дата, время завершения аренды/ поездки
    // @ts-ignore
    ['Дата, время завершения аренды/ поездки ', formatTime(trip.trip?.rentFinishedAt) ?? '-'],
    // Плановое время брони/аренды
    ['Плановое время брони/аренды ', getTimeString(trip.expected.time) ?? '-'],
    // Фактическое время брони
    // @ts-ignore
    ['Фактическое время брони ', getTimeString(trip.trip?.reserveTime) ?? '-'],
    // Плановое время поездки
    // @ts-ignore
    ['Плановое время поездки ', getTimeString(trip.trip?.drivingTime) ?? '-'],
    // Фактическое время ожидания (мин)
    // @ts-ignore
    ['Фактическое время ожидания (мин) ', getTimeString(trip.trip?.parkingTime) ?? '-'],
    // Плановый пробег (км)
    ['Плановый пробег (км) ', formatDistance(trip.expected.distance) ?? '-'],
    // Фактический пробег (км)
    // @ts-ignore
    ['Фактический пробег (км) ', formatDistance(trip.trip?.drivingLength) ?? '-'],
    // Плановая стоимость брони (руб)
    // @ts-ignore
    ['Плановая стоимость брони (руб) ', formatRubles(trip.trip?.reserveTimeCost) ?? '-'],
    // Фактическая стоимость поездки (руб)
    // @ts-ignore
    ['Фактическая стоимость поездки (руб) ', formatRubles(trip.trip?.drivingTimeCost) ?? '-'],
    // Фактическая стоимость ожидания (руб)
    // @ts-ignore
    ['Фактическая стоимость ожидания (руб) ', formatRubles(trip.trip?.parkingTimeCost) ?? '-'],
    // Дополнительная стоимость за пробег (руб)
    // @ts-ignore
    ['Дополнительная стоимость за пробег (руб) ', formatRubles(trip.trip?.drivingLengthCost) ?? '-'],
    // Итоговая плановая стоимость аренды (руб)
    ['Итоговая плановая стоимость аренды (руб) ', formatRubles(trip.expected.cost) ?? '-'],
    // Итоговая фактическая стоимость аренды (руб)
    // @ts-ignore
    ['Итоговая фактическая стоимость аренды (руб) ', formatRubles(trip.trip?.totalCost) ?? '-'],
  ];
};
