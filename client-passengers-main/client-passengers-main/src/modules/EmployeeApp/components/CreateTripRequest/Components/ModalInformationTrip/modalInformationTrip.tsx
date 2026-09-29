/* eslint-disable @typescript-eslint/no-explicit-any */

import { Modal, Button } from 'antd';
import { FormInstance } from 'antd/es/form/Form';

import { convertAddress } from 'utils/convertAddress';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';
import { formatFullName } from 'utils/formatFullName';
import React, { useEffect, useState } from 'react';
import User from 'shared/components/Images/user.png';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import Taxi from '../../../../../../shared/components/Images/car_yellow.svg';
import Close from '../../../../../../shared/components/Images/Close.svg';
import Pers from '../../../../../../shared/components/Images/pers.svg';
import { TripInfoRowsLayout } from '../SuitableTrips/TripInfoRowsLayout';
import modalInformationTrip from './modalInformationTrip.module.scss';
import PassengersItem from './PassengersItem/passengersItem';
import { StoreNames } from 'ioc/ioc.storeNames';

interface ModalInformationTripProps {
  onJoinCoopTrip: (item: TripSuitableModel) => void;
  visible: boolean;
  onCancel: () => void;
  item: TripSuitableModel;
  form: FormInstance;
  colorSetting: any;
}

const ModalInformationTrip: React.FC<ModalInformationTripProps> = ({
  onJoinCoopTrip,
  visible,
  onCancel,
  item,
  form,
  colorSetting,
}) => {
  const landing = item.readableTimeStops[0];
  const startAdress = convertAddress(item?.readableTimeStops.filter(el => el.eventType === 'Посадка')[0].address);
  const endAdress = convertAddress(item?.readableTimeStops.filter(el => el.eventType === 'Высадка')[0].address);
  const phoneNumber = formatPhoneNumber(item?.employeePassengers[0].mobilePhone);
  const fullName = formatFullName(
    item?.employeePassengers[0].firstName,
    item?.employeePassengers[0].lastName,
    item?.employeePassengers[0].patronymic
  );
  const passengers = item?.requests.filter(el => el.sharedRideOwner === false);
  const car = item?.requests.find(el => el.sharedRideOwner === true);
  const carModel = car?.car?.brandName ?? '';
  const carNumber = car?.car?.registrationNumber ?? '';
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const coopTrip = form.getFieldValue('coopTrip');

  const [avatar, setAvatar] = useState<string>('');

  useEffect(() => {
    tripStore.getUserAvatart(item.employeePassengers[0].userId).then(setAvatar);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Modal
      visible={visible}
      onCancel={onCancel}
      className={modalInformationTrip.modal}
      footer={[
        // eslint-disable-next-line react/jsx-key
        <div className={modalInformationTrip.footer}>
          <div className={modalInformationTrip.footerCost}>
            <span>Стоимость</span>
            <div className={modalInformationTrip.tripInfoCost}>
              <TripInfoRowsLayout
                item={item}
                form={form}
                colorSetting={colorSetting}
              />
            </div>
          </div>
          <div className={modalInformationTrip.footerButton}>
            <Button
              key="cancel"
              type="text"
              onClick={onCancel}
            >
              Отмена
            </Button>

            <Button
              key="book"
              type="primary"
              onClick={() => onJoinCoopTrip(item)}
              disabled={tripStore.progressApplicationCreationRequest || !coopTrip}
            >
              Забронировать место
            </Button>
          </div>
        </div>,
      ]}
    >
      <div className={modalInformationTrip.cardWrapper}>
        <div className={modalInformationTrip.header}>
          <div className={modalInformationTrip.numberApplication}>{`Поездка №${item.requests[0].humanReadableId}`}</div>
          <div className={modalInformationTrip.cancel} onClick={onCancel}>
            <img alt="Close" src={Close} />
          </div>
        </div>
        <div className={modalInformationTrip.main}>
          <div className={modalInformationTrip.informationDate}>{landing?.startTime}</div>
          <div className={modalInformationTrip.informationRout}>
            <div className={modalInformationTrip.informationRoutTitle}>Маршрут</div>
            <div className={modalInformationTrip.informationRoutMain}>
              <div className={modalInformationTrip.informationRoutImg}>
                <div className={modalInformationTrip.informationRoutImgStart}>
                  <span>A</span>
                </div>
                <div className={modalInformationTrip.informationRoutImgLine} />
                <div className={modalInformationTrip.informationRoutImgEnd}>
                  <span>B</span>
                </div>
              </div>
              <div className={modalInformationTrip.informationRoutPath}>
                <div className={modalInformationTrip.informationRoutStart}>
                  <span>начало поездки</span>
                  <span>{startAdress}</span>
                </div>
                <div className={modalInformationTrip.informationRoutEnd}>
                  <span>конец поездки</span>
                  <span>{endAdress}</span>
                </div>
              </div>
            </div>
          </div>
          {item?.transportType === 'TAXI' ? (
            <div className={modalInformationTrip.informationCar}>
              <span className={modalInformationTrip.informationCarTitle}>Такси</span>
              <div className={modalInformationTrip.informationCarWrapper}>
                <div className={modalInformationTrip.informationCarMain}>
                  <img src={Taxi} alt="" />
                  <div className={modalInformationTrip.informationCarModel}>
                    <span>Тариф</span>
                    <span>{TaxiClassTitlesEnum[item.taxiClass]}</span>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className={modalInformationTrip.informationCar}>
              <span className={modalInformationTrip.informationCarTitle}>Автомобиль</span>
              <div className={modalInformationTrip.informationCarWrapper}>
                <div className={modalInformationTrip.informationCarMain}>
                  <img src={Pers} alt="" />
                  <div className={modalInformationTrip.informationCarModel}>
                    <span>Автомобиль</span>
                    <span>{carModel}</span>
                  </div>
                </div>
                <div className={modalInformationTrip.informationCarNumber}>
                  <span>гос.номер</span>
                  <span>{carNumber}</span>
                </div>
              </div>
            </div>
          )}
          <div className={modalInformationTrip.informationDriver}>
            <span className={modalInformationTrip.informationDriverTitle}>
              {item.transportType === 'TAXI' ? 'Инициатор' : 'Водитель'}
            </span>
            <div className={modalInformationTrip.informationDriverWrapper}>
              <div className={modalInformationTrip.informationDriverMain}>
                <img alt="avatar" src={avatar || User} />
                <div className={modalInformationTrip.informationDriverInfo}>
                  <span className={modalInformationTrip.informationDriverInfoName}>{fullName}</span>
                  <span className={modalInformationTrip.informationDriverInfoPosition}>
                    {item.requests[0].employee?.positionName}
                  </span>
                  <span className={modalInformationTrip.informationDriverInfoNumber}>
                    {`Таб. №${item.requests[0].employee?.personnelNumber}`}
                  </span>
                </div>
              </div>
              <div className={modalInformationTrip.informationDriverNumber}>
                <span>Тел.:</span>
                <span>{phoneNumber}</span>
              </div>
            </div>
          </div>
          <div className={modalInformationTrip.informationPassengers}>
            <span className={modalInformationTrip.informationPassengersTitle}>Пассажиры</span>
            <div className={modalInformationTrip.informationPassengersList}>
              {passengers.map(passenger => (
                <PassengersItem key={passenger.id} item={passenger} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default ModalInformationTrip;
