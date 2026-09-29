import { Space } from 'antd-mobile';
import { PassTrip } from 'api/services/Trips/Trips.types';
import { FC, ReactNode } from 'react';
import styles from './TripInfo.module.scss';
import dayjs from 'dayjs';
import { EMPTY } from 'constants/app.constants';

interface Data {
  title: string;
  value: (trip: PassTrip) => ReactNode;
}

const data: Data[] = [
  {
    title: 'Количество пассажиров',
    value: trip => trip.passengerCount,
  },
  {
    title: 'Дата и время рейса/поезда',
    value: trip => trip.information?.dateFlight
      ? dayjs(trip.information.dateFlight).format('DD.MM.YYYY HH:mm')
      : EMPTY,
  },
  {
    title: 'Номер рейса/поезда',
    value: trip => trip.information?.numberFlight ?? EMPTY,
  },
  {
    title: 'ФИО дополнительного контактного лица',
    value: trip => trip.information?.addContactFIO ?? EMPTY,
  },
  {
    title: 'Телефон дополнительного контактного лица',
    value: trip => trip.information?.addContactPhone ?? EMPTY,
  },
  {
    title: 'Номер в гостинице',
    value: trip => trip.information?.phoneHotel ?? EMPTY,
  },
  {
    title: 'Количество багажа',
    value: trip => trip.information?.bugsComment ?? EMPTY,
  },
  {
    title: 'Негабаритный багаж',
    value: trip => trip.information?.bugsOversizedComment ?? EMPTY,
  },
  {
    title: 'Детское кресло',
    value: trip => {
      const childSeatDetails = trip.information?.childSeatDetails;

      if (
        !childSeatDetails?.newborn
        && !childSeatDetails?.group1
        && !childSeatDetails?.group2
        && !childSeatDetails?.booster
      ) {
        return EMPTY;
      }

      return (
        <>
          {childSeatDetails?.newborn && (
          <div>
            Кресло, до 9 мес. —
            {childSeatDetails.newborn}
            {' '}
            шт.
          </div>
          )}
          {childSeatDetails?.group1 && (
          <div>
            Кресло, от 9 мес. до 4 лет —
            {childSeatDetails.group1}
            {' '}
            шт.
          </div>
          )}
          {childSeatDetails?.group2 && (
          <div>
            Кресло —
            {childSeatDetails.group2}
            {' '}
            шт.
          </div>
          )}
          {childSeatDetails?.booster && (
          <div>
            Бустер, 6-12 лет —
            {childSeatDetails.booster}
            {' '}
            шт.
          </div>
          )}
        </>
      );
    },
  },
  {
    title: 'Желаемый тип ТС',
    value: trip => trip.information?.typeVehicle ?? EMPTY,
  },
  {
    title: 'Перевозка животного',
    value: trip => trip.information?.animalComment ?? EMPTY,
  },
];

const TripInfo: FC<{ trip: PassTrip }> = ({ trip }) => {
  return (
    <Space direction="vertical" className={styles.container}>
      {data.map(info => (
        <div key={info.title}>
          <div className={styles.title}>{info.title}</div>
          <div className={styles.desc}>{info.value(trip)}</div>
        </div>
      ))}
    </Space>
  );
};

export default TripInfo;
