/* eslint-disable  @typescript-eslint/no-explicit-any */
import { useEffect, useState } from 'react';
import { Divider } from 'antd-mobile';
import Modal from 'components/Modal/Modal';
import styles from './ChangeDeparture.module.scss';
import Autocomplete from './AutoComplete';
import { LatLngTuple } from 'api/services/Geo/Geo.types';
import { useGeoService } from 'api/services/Geo/Geo';
import { TRANSPORT_SERVICE_TYPES } from 'constants/geo.constants';
import { useAppStore } from 'stores/stores.context';
import dayjs from 'dayjs';

interface Props {
  isChangeDepartureVisible: boolean;
  handleChange: () => void;
  trip: any;
  clickedPoints: LatLngTuple;
}

const ChangeDeparture = ({
  isChangeDepartureVisible, handleChange, trip, clickedPoints,
}: Props) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isFirstStep, setIsFirstStep] = useState(true);
  const [isPointAvailable, setIsPointAvailable] = useState(true);
  const [newDeparture, setNewDeparture] = useState<LatLngTuple>([0, 0]);

  const { mapStore } = useAppStore();

  const { getRoute, getAddressByCoordinates } = useGeoService();

  useEffect(() => {
    setIsVisible(isChangeDepartureVisible);
  }, [isChangeDepartureVisible]);

  useEffect(() => {
    if (clickedPoints[0] != 0 && clickedPoints[1] != 0)
      getAddressByCoordinates(clickedPoints).then(resp => {
        setNewDeparture([resp[0].latitude, resp[0].longitude]);
        setIsPointAvailable(true);
      }).catch(() => {
        setIsPointAvailable(false);
      });
  }, [clickedPoints[0], clickedPoints[1]]);

  const onClose = () => {
    handleChange();
    setIsVisible(false);
    setIsFirstStep(true);
  };

  const onDepartureChange = () => {
    setIsFirstStep(false);
  };

  const handleNewRoute = () => {
    getRoute({
      coordinates: [
        { latitude: newDeparture[0], longitude: newDeparture[1] },
        ...trip.waypoints.map(({ longitude, latitude }) => ({
          longitude,
          latitude,
        }))], transportServiceType: TRANSPORT_SERVICE_TYPES.EMPLOYEE_TRANSPORTATION,
    }).then(resp => {
      mapStore.setRoutes(resp.segments);
      mapStore.setCenter([newDeparture[1], newDeparture[0]]);
      mapStore.setExpectedTime(resp.time);
      mapStore.setExpectedDistance(resp.distance);
      const time = dayjs().add(Math.round(resp.time), 'ms');
      mapStore.setExpectedArrivalTime(time);
    });
    setIsVisible(false);
    setIsFirstStep(true);
    handleChange();
  };

  return (
    <>
      {isFirstStep
        ? (
          <Modal
            visible={isVisible}
            className={styles.modal}
            onClose={close}
            content={(
              <div className={styles.container}>
                <span className={styles.header}>Проезд отсюда</span>
                {!!isPointAvailable
                && (
                <div className={styles.buttonBlock}>
                  <Divider className={styles.divider} />
                  <span className={styles.option} onClick={handleNewRoute}>Да</span>
                </div>
                )}
                <div className={styles.buttonBlock}>
                  <Divider className={styles.divider} />
                  <span className={styles.option} onClick={onDepartureChange}>Ввести адрес вручную</span>
                </div>
                <div className={styles.buttonBlock}>
                  <Divider className={styles.divider} />
                  <span className={styles.option} onClick={onClose}>Нет</span>
                </div>
              </div>
                        )}
          />
        )
        : (
          <Modal
            visible={isVisible}
            className={styles.modal}
            onClose={close}
            content={(
              <div className={styles.container}>
                <Autocomplete handleDepartureChange={setNewDeparture} />
                <div className={styles.buttonBlock}>
                  <Divider className={styles.divider} />
                  <span className={styles.option} onClick={handleNewRoute}>Отсюда</span>
                </div>
                <div className={styles.buttonBlock}>
                  <Divider className={styles.divider} />
                  <span className={styles.option} onClick={onClose}>Отмена</span>
                </div>
              </div>
                        )}
          />
        )}
    </>

  );
};

export default ChangeDeparture;
