import { useMemo } from 'react';
import moment from 'moment';
import { PassTrip } from 'api/trips/trips.types';
import { DATE_FORMAT, EMPTY_CELL_CONTENT } from 'constants/app.constants';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { getFullName } from 'utils/getFullName';

export const useTransferData = (trip: PassTrip) => {
  const data = useMemo(() => [
    {
      title: 'Количество пассажиров',
      desc: trip.passengerCount,
    },
    {
      title: 'ФИО клиента',
      desc: trip.requests[0]?.author
        ? getFullName(trip.requests[0].author)
        : trip.waypoints[0]?.contact?.name,
    },
    {
      title: 'Телефон клиента',
      desc: trip.requests[0]?.author?.mobilePhone
        ? formatPhoneNumber(trip.requests[0].author.mobilePhone)
        : formatPhoneNumber(trip.waypoints[0]?.contact?.phone),
    },
    {
      title: 'ФИО доп. контактного лица',
      desc: trip.requests[0]?.information?.addContactFIO ?? trip.information?.addContactFIO ?? EMPTY_CELL_CONTENT,
    },
    {
      title: 'Телефон доп. контактного лица',
      desc: trip.requests[0]?.information?.addContactPhone ?? trip.information?.addContactPhone ?? EMPTY_CELL_CONTENT,
    },
    {
      title: 'Номер рейса/поезда',
      desc: trip.requests[0]?.information?.numberFlight ?? trip.information?.numberFlight ?? EMPTY_CELL_CONTENT,
    },
    {
      title: 'Дата и время рейса/поезда',
      desc: trip.requests[0]?.information?.dateFlight
        ? moment
          .utc(trip.requests[0].information.dateFlight)
          .utcOffset(trip.requests[0].timeZone!)
          .format(DATE_FORMAT.DATE_WITH_TIME)
        : trip.information?.dateFlight
          ? moment(trip.information.dateFlight).format(DATE_FORMAT.DATE_WITH_TIME)
          : EMPTY_CELL_CONTENT,
    },
    {
      title: 'Номер в гостинице',
      desc: trip.requests[0]?.information?.phoneHotel
      ?? trip.information?.phoneHotel ?? EMPTY_CELL_CONTENT,
    },
    {
      title: 'Количество багажа',
      desc: (trip.requests[0]?.information?.bugs || trip.information?.bugs)
        ? (trip.requests[0]?.information?.bugsComment ?? trip.information?.bugsComment)
        : EMPTY_CELL_CONTENT,
    },
  ], [trip]);

  return { data };
};
