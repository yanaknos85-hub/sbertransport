import React, { useMemo } from 'react';
import moment from 'moment';
import { CheckinInfo, PassTrip } from 'api/trips/trips.types';
import { DATE_FORMAT, EMPTY_CELL_CONTENT, TransportTypeDescriptions } from 'constants/app.constants';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { convertToRubles } from 'utils/convertToRubles';
import { getFullName } from 'utils/getFullName';
import {
  GroupTransferClass, GroupTransferClassTitles, TaxiClass, TaxiClassDescriptions
} from 'constants/trips.constants';
import { formatRubles } from 'utils/formatRubles';
import useSegments from 'modules/Trip/sections/RouteCard/hooks/useSegments';

export const useData = (trip: PassTrip, checkinInfo?: CheckinInfo) => {
  const isFinishedTrip = trip.status === TRIP_STATUSES.ORDER_FINISHED;
  const {
    planDistance,
    factDistance,
    isFetching,
  } = useSegments(trip.id, isFinishedTrip);

  const expectedData = useMemo(() => {
    const isSameTimeZone = moment.parseZone(trip.expectedStartTime).utcOffset() === moment().utcOffset();

    const waitingDuration = moment.duration();
    trip.requests.forEach(request => {
      request.waypoints?.forEach(waypoint => waitingDuration.add(moment.duration(waypoint.waitingTime)));
    });

    return [
      {
        title: 'Дата и время отправления',
        desc: isSameTimeZone
          ? moment(trip.expectedStartTime).format(DATE_FORMAT.DATE_WITH_TIME)
          : (
            <>
              <div>
                Клиент:
                {moment.parseZone(trip.expectedStartTime).format(DATE_FORMAT.DATE_WITH_TIME)}
              </div>
              <div>
                Диспетчер:
                {moment(trip.expectedStartTime).format(DATE_FORMAT.DATE_WITH_TIME)}
              </div>
            </>
          ),
      },
      {
        title: 'Контрольный срок, дата и время',
        desc: isSameTimeZone
          ? moment(trip.expectedStartTime)
            .add(15, 'm') // от аналитика: рассчитывать контрольный срок +15 мин от времени отправления
            .format(DATE_FORMAT.DATE_WITH_TIME)
          : (
            <>
              <div>
                Клиент:
                {moment.parseZone(trip.expectedStartTime).add(15, 'm').format(DATE_FORMAT.DATE_WITH_TIME)}
              </div>
              <div>
                Диспетчер:
                {moment(trip.expectedStartTime).add(15, 'm').format(DATE_FORMAT.DATE_WITH_TIME)}
              </div>
            </>
          ),
      },
      {
        title: 'Класс автомобиля',
        desc: trip.taxiClass
          ? TaxiClassDescriptions[trip.taxiClass as TaxiClass] ?? trip.taxiClass
          : trip.requests[0]?.groupTransferClass
            ? GroupTransferClassTitles[trip.requests[0].groupTransferClass as GroupTransferClass]
            ?? trip.requests[0].groupTransferClass
            : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Вид транспорта',
        desc: trip.requests[0]?.transportType
          ? TransportTypeDescriptions[trip.requests[0].transportType]
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Предварительный километраж, км',
        desc: isFetching ? '...' : planDistance?.toFixed(2) ?? EMPTY_CELL_CONTENT,
      },
      {
        title: 'Предварительная стоимость, руб',
        desc: trip.expectedCost ? formatRubles(convertToRubles(trip.expectedCost)) : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Предварительное время поездки, мин',
        desc: trip.expectedTime
          ? Math.round(trip.expectedTime / 60)
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Ожидание, указанное пассажиром, мин',
        desc: waitingDuration.asMinutes() || EMPTY_CELL_CONTENT,
      },
    ];
  }, [trip, planDistance, isFetching]);

  const factData = useMemo(() => {
    const isSameTimeZone = moment.parseZone(trip.expectedStartTime).utcOffset() === moment().utcOffset();

    return [
      {
        title: 'Дата и время получения заявки перевозчиком',
        desc: trip.creationTime
          ? isSameTimeZone
            ? moment(trip.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)
            : (
              <>
                <div>
                  Клиент:
                  {moment.parseZone(trip.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
                <div>
                  Диспетчер:
                  {moment(trip.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
              </>
            )
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Дата и время закрытия заявки',
        desc: trip.factEndTime
          ? isSameTimeZone
            ? moment(trip.factEndTime).format(DATE_FORMAT.DATE_WITH_TIME)
            : (
              <>
                <div>
                  Клиент:
                  {moment.parseZone(trip.factEndTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
                <div>
                  Диспетчер:
                  {moment(trip.factEndTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
              </>
            )
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'ФИО диспетчера',
        desc: trip.dispatcher ? getFullName(trip.dispatcher) : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Время выезда',
        desc: trip.factStartTime
          ? isSameTimeZone
            ? moment(trip.factStartTime).format(DATE_FORMAT.DATE_WITH_TIME)
            : (
              <>
                <div>
                  Клиент:
                  {moment.parseZone(trip.factStartTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
                <div>
                  Диспетчер:
                  {moment(trip.factStartTime).format(DATE_FORMAT.DATE_WITH_TIME)}
                </div>
              </>
            )
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Километраж, км',
        desc: isFetching ? '...' : (factDistance ? (
          <span>
            {factDistance.toFixed(2)}
            {' '}
            км
          </span>
        ) : EMPTY_CELL_CONTENT),
      },
      {
        title: 'Стоимость, руб',
        desc: trip.factCost ? formatRubles(convertToRubles(trip.factCost)) : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Поездка, мин',
        desc: checkinInfo?.tripDuration
          ? checkinInfo.tripDuration.days * 24 * 60 + checkinInfo.tripDuration.hours * 60
          + checkinInfo.tripDuration.minutes
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Ожидание, мин',
        desc: trip.driverWaitingTime ? Math.round(trip.driverWaitingTime / 60) : EMPTY_CELL_CONTENT,
      },
    ];
  }, [trip, checkinInfo, factDistance, isFetching]);

  return { expectedData, factData };
};
