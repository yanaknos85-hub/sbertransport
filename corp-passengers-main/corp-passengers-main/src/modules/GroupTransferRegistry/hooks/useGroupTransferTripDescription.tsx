import { UUID } from 'utils/io-ts';
import { useSingleContractor } from 'api/contractors';
import { formatTime, getTimeString } from 'utils/formatTime';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { convertToRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { useGroupTransferClassName } from './useGroupTransferClassName';
import { GroupTransferReportItem } from 'stores/GroupTransferRegistry/GroupTransferRegistry';
import { fullNameLastFirstPat } from '../utils/fullNameLastFirstPat';
import { GroupTransferStatusNames, GroupTransferStatuses, VALUE_NOT_FOUND } from '../constants/groupTransfer.constants';
import { Records } from '../../TaxiRegistry/types/types';
import {
  getIndividualTripAddresses,
  getIndividualTripApprovedBy
} from 'utils/reportsUtils';
import { DATE_FORMAT } from 'constants/constants.app';

export const useGroupTransferTripDescriptions = (
  tripRequest: TripResponse,
  tripReports: GroupTransferReportItem,
  labels: Record<string, string>
): {
  generalInfo: Records;
  userInfo: Records;
  tripInfo: Records;
  sctructureInfo: Records;
  planFactInfo: Records;
  additionalInfo: Records;
} => {
  const contractor = useSingleContractor(tripReports?.contractor?.id as UUID).data;

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    tripRequest.expected.waypoints
  );

  return {
    generalInfo: [
      ['Наименование организации ТБ', tripReports.organizationOfficialName || '-'],
      [labels.requestIdVisible, tripRequest.humanReadableId],
      ['МВЗ', tripReports.costCenter ?? '-'],
      [labels.creationDateTimeVisible, formatTime(tripReports.creationTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.requestStatusVisible, tripReports.status && (tripReports.status in GroupTransferStatuses)
        ? GroupTransferStatusNames[tripReports.status as GroupTransferStatuses]
        : VALUE_NOT_FOUND],
      ['Желаемая дата и время отправления', '-'],
      [labels.groupTransferClassVisible, useGroupTransferClassName(tripReports.groupTransferClass)],
    ] as Records,

    userInfo: [
      ['Табельный номер заявителя', tripReports?.author?.personnelNumber],
      ['ФИО заявителя', fullNameLastFirstPat(tripReports.author)],
      ['Количество пассажиров', tripReports?.passengerCount ?? '-'],
      ['Цель поездки', tripRequest.purpose?.label ?? '-'],
      ['ID лимита', tripReports.limit?.humanReadableId ?? '-'],
    ] as Records,

    tripInfo: [
      [labels.waypointFromVisible, departureAddress],
      ['Промежуточные адреса', intermediateAddress],
      ['Количество точек в маршруте', tripRequest.expected.waypoints.length],
      ['Адрес прибытия', destinationAddress],
      ['Дата и время выполнения заявки', formatTime(tripReports.finishedTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Дата закрытия обращения', formatTime(tripReports.requestClosedDatetime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Контрольный срок подачи ТС (расчетный)', formatTime(tripReports.deadline, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Фактическое дата и время прибытия ТС', formatTime(tripReports.driverArrivedDatetime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      ['Нарушение КС', tripReports.deadlineViolation],
    ] as Records,

    sctructureInfo: [
      ['Код подразделения. OE', tripReports?.department?.code],
      ['Подразделение 1 уровня', tripReports.passengerDepartment1 ?? '-'],
      ['Подразделение 2 уровня', tripReports.passengerDepartment2 ?? '-'],
      ['Подразделение 3 уровня', tripReports.passengerDepartment3 ?? '-'],
      ['Подразделение 4 уровня', tripReports.passengerDepartment4 ?? '-'],
      ['Подразделение 5 уровня', tripReports.passengerDepartment5 ?? '-'],
      ['Подразделение 6 уровня', tripReports.passengerDepartment6 ?? '-'],
      ['Должность пассажира', tripRequest.passenger.positionName],
      ['Табельный номер пассажира', tripRequest.passenger.personnelNumber],
      ['ФИО пассажира', fullNameLastFirstPat(tripRequest.passenger)],
    ] as Records,

    planFactInfo: [
      ['Исполнитель', contractor?.name ?? '-'],
      ['Предварительный километраж в заявке, км', formatDistance(tripRequest.expected.distance)],
      ['Фактический километраж в поездке, км', formatDistance(tripReports.factData?.tripFactDistance)],
      ['Предварительная стоимость в заявке, руб', convertToRubles(tripRequest.expected.cost)],
      ['Фактическая стоимость в поездке, руб', tripReports.factData?.tripFactPrice ? convertToRubles(tripReports.factData?.tripFactPrice) : '-'],
      ['Предварительное время в заявке, час/мин', getTimeString(tripRequest.expected.time)],
      ['Фактическое время поездки в поездке, час/мин', tripReports.factData?.tripFactDuration ? getTimeString(tripReports.factData?.tripFactDuration) : '-'],
      ['Суммарное фактическое время ожидания, мин', getTimeString(tripReports.factData?.tripFactWaitTime)],
    ] as Records,

    additionalInfo: [
      ['Табельный номер согласующего заявку', tripRequest.approvedBy?.personnelNumber],
      [labels.approvedByFioVisible, getIndividualTripApprovedBy(tripRequest.approvedBy)],
      ['Инициатор совместной поездки', tripRequest.coopTrip ? 'Да' : 'Нет'],
      ['Оценки поездки пользователем', tripRequest.requestRating?.rating ?? 'Отсутствует'],
      [labels.requestRatingCommentVisible, tripRequest.requestRating?.ratingComment ?? 'Отсутствует'],
    ] as Records,
  };
};
