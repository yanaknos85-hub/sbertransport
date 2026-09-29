/* eslint-disable no-nested-ternary */
import React, { useState } from 'react';
import { Modal, Button, Input } from 'antd';

import { WaypointField } from 'shared/models/geo/types';
import {
  AbsenceRequestData, DeleteWaypointData, useAbsenceReason, useDeleteWaypoint
} from 'api/check-in';
import { useCurrentTripRequest } from 'shared/hooks/trip';
import Warning from 'shared/components/Images/warning.svg';

import './item.scss';

const { TextArea } = Input;

interface ModalInformationTripProps {
  visible: boolean;
  onCancel: () => void;
  addresses: WaypointField[];
  numberAddress: number;
  requestId: string | undefined;
}

const ModalCheckIn: React.FC<ModalInformationTripProps> = ({
  visible,
  onCancel,
  addresses,
  numberAddress,
  requestId,
}) => {
  const [makingStop, setMakingStop] = useState(false);
  const [notMakingStop, setNotMakingStop] = useState(false);
  const [absenceReasonText, setAbsenceReasonText] = useState('');
  const { refetchRequestTrip } = useCurrentTripRequest();

  const address = addresses.filter((el, index) => index === numberAddress)[0];

  const trimmedReason = (absenceReasonText || '').trim();
  const isReasonValid = trimmedReason.length >= 15;

  const [absenceReason] = useAbsenceReason();

  const [deleteWaypoint] = useDeleteWaypoint();

  const prepareRequestData = (): AbsenceRequestData => {
    return {
      requestId: requestId || '',
      latitude: address?.latitude,
      longitude: address?.longitude,
      absenceReason: absenceReasonText,
      orderingIndex: addresses.findIndex(el => el.fieldName === address.fieldName),
    };
  };

  const prepareDeleteWaypointData = (): DeleteWaypointData => {
    return {
      requestId: requestId || '',
      latitude: address?.latitude,
      longitude: address?.longitude,
      orderingIndex: addresses.findIndex(el => el.fieldName === address.fieldName),
    };
  };

  const sendAbsenceReasons = () => {
    const data = prepareRequestData();
    absenceReason(data).then(() => refetchRequestTrip());
  };

  const sendDeleteWaypoint = () => {
    const data = prepareDeleteWaypointData();
    deleteWaypoint(data).then(() => refetchRequestTrip());
  };

  return (
    <Modal
      visible={visible}
      onCancel={onCancel}
      className="modal_checkIn"
      footer={[]}
    >
      <div className="cardWrapper">
        <div className="header">
          <div className="numberApplication">
            {makingStop ? 'Укажите причину' : notMakingStop ? 'Удалить точку из маршрута?' : 'Не нашли вас на точке'}
          </div>
        </div>
        <div className="main">
          {makingStop
            ? `Почему система не зафиксировала вас на точке ${address.addressString}?`
            : notMakingStop
              ? `Вы точно хотите удалить адрес ${address.addressString}?`
              : `Делали ли вы остановку на адресе ${address.addressString}?`}
          {makingStop && (
            <div className="warning">
              <div>
                <img src={Warning} alt="Warning" />
              </div>
              <span>
                При отключении геолокации срок исполнения заявки может
                увеличиться до 25 дней
              </span>
            </div>
          )}
          {makingStop && (
            <TextArea
              rows={4}
              maxLength={255}
              showCount
              placeholder="Укажите причину отсутствия"
              value={absenceReasonText}
              defaultValue={absenceReasonText}
              className={`inputWayPoint${!isReasonValid && absenceReasonText ? ' inputWayPoint_error' : ''}`}
              onChange={el => setAbsenceReasonText(el.target.value)}
              disabled={address.checkinAutomatic || address.checkinManual}
            />
          )}
          {makingStop && !isReasonValid && absenceReasonText && (
            <div className="errorMessage">
              Пожалуйста, укажите причину не менее 15 символов. Пробелы не учитываются.
            </div>
          )}
        </div>
      </div>
      <div className="footer">
        {!makingStop && !notMakingStop && (
          <div className="footerButton">
            <div className="buttonStop" onClick={() => setMakingStop(true)}>
              делал(а) остановку
            </div>
            <div className="buttonStop" onClick={() => setNotMakingStop(true)}>
              не делал(а) остановку
            </div>
          </div>
        )}
        <div className="footerButtonNext">
          {makingStop && (
            <>
              <div className="cancel" onClick={onCancel}>
                Отмена
              </div>
              <Button
                disabled={!isReasonValid}
                className="approve"
                onClick={sendAbsenceReasons}
              >
                Подтвердить
              </Button>
            </>
          )}
          {notMakingStop && (
            <>
              <div className="cancel" onClick={onCancel}>
                Отмена
              </div>
              <div className="delete" onClick={sendDeleteWaypoint}>
                Удалить
              </div>
            </>
          )}
        </div>
      </div>
    </Modal>
  );
};

export default ModalCheckIn;
