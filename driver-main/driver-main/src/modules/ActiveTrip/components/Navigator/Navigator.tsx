import { FC } from 'react';
import Button from 'components/Button/Button';
import { ReactComponent as TwoGis } from 'assets/icons/2gis.svg';
import { ReactComponent as Alert } from 'assets/icons/alert.svg';
// import Yandex from 'assets/images/transport.png';
import styles from './Nvigator.module.scss';

const get2gisUrl = (longitude?: number, latitude?: number) => `https://2gis.ru/routeSearch/to/${longitude},${latitude}/go`;

interface NavigatorProps {
  close: () => void;
  longitude?: number;
  latitude?: number;
}

const Navigator: FC<NavigatorProps> = ({
  close,
  longitude,
  latitude,
}) => {
  return (
    <div className={styles.navigator}>
      <div className={styles.title}>Построить маршрут в навигаторе?</div>
      <div className={styles.text}>
        Выберите приложение, в котором вы хотите построить маршрут поездки
      </div>
      <div className={styles.apps}>
        <a
          href={get2gisUrl(longitude, latitude)}
          target="_blank"
          rel="noreferrer"
          onClick={close}
        >
          <TwoGis />
        </a>
        {/* Скрыто, пока не получим ключ доступа для яндекса */}
        {/* <a
          href={}
          target="_blank"
          rel="noreferrer"
          onClick={close}
        >
          <img src={Yandex} alt="Yandex.Navigator" />
        </a> */}
      </div>
      <div className={styles.alert}>
        <Alert />
        <div>Не закрывайте вкладку «СберТранспорт» для отметки контрольных точек маршрута</div>
      </div>
      <Button
        color="primary"
        className={styles.button}
        block
        onClick={close}
      >
        Продолжить без навигатора
      </Button>
    </div>
  );
};

export default Navigator;
