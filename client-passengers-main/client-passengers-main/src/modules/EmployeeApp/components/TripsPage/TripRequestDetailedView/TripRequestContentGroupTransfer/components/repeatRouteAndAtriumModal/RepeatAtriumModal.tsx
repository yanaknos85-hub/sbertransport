/* eslint-disable no-unsafe-optional-chaining */
import { Button, DatePicker, Modal } from 'antd';
import { observer } from 'mobx-react';
import React, {
  FC,
  useState
} from 'react';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment, { Moment } from 'moment';
import cn from 'classnames';

import { DATE_FORMAT } from 'constants/constants.app';

import Close from 'shared/components/Images/Close.svg';
import Up from 'shared/components/Images/Up.svg';

import RouteMap from 'utils/RouteMap/RouteMap';
import { TripRequestModel } from 'stores/Trip/models';
import { declOfNum } from '../../../utils/declOfNum';
import { parseNumber } from 'utils/parseNumber';
import { zoneTime } from 'utils/trips/times';

import styles from './repeatAtrium.module.scss';
import '../../override.scss';

interface RepeatRouteAndAtriumModalProps {
  visible: boolean;
  onCancel: () => void;
  reloadTrip: () => void;
  request: TripRequestModel;
  setChoosingTime: React.Dispatch<React.SetStateAction<Moment | null>>;
  choosingTime: Moment | null;
}

export const RepeatRouteAndAtriumModal: FC<RepeatRouteAndAtriumModalProps> = observer(
  ({
    visible,
    onCancel,
    reloadTrip,
    request,
    setChoosingTime,
    choosingTime,
  }): JSX.Element => {
    const [visibleInformationTrip, setVisibleInformationTrip] = useState(true);
    const [visibleAdditionalParameters, setVisibleAdditionalParameters] = useState(true);
    const [visibleCommentPurpose, setVisibleCommentPurpose] = useState(true);

    const getDisabledDate = (value: moment.Moment): boolean => {
      const now = moment().add(-1, 'days');
      return value < now || value > now.add(1, 'years');
    };

    const passengerCount = request.passengerCount;
    const passengerName = request.passenger.fullName;
    const passengerMobilePhone = request.passenger.mobilePhone;
    const bugsOversizedComment = request.information?.bugsOversizedComment;
    const childSeatDetails = request.information?.childSeatDetails;
    const clientFullName = request.information?.addContact?.split('+')[0];
    const clientInfoPhone = request.information?.addContact?.split('+')[1];
    const dateFlight = request.information?.dateFlight;
    const numberFlight = request.information?.numberFlight;
    const phoneHotel = request.information?.phoneHotel;
    const bugsComment = request.information?.bugsComment;
    const typeVehicle = request.information?.typeVehicle;
    const animalComment = request.information?.animalComment;

    return (
      <div onClick={e => e.stopPropagation()}>
        <Modal
          open={visible}
          onCancel={onCancel}
          className={styles.repeatRouteAndAtriumModal}
          footer={false}
        >
          <div className="cardWrapper">
            <div className="header">
              <div className="numberApplication">
                Повторить заявку
              </div>
              <div className="cancel" onClick={onCancel}>
                <img alt="Close" src={Close} />
              </div>
            </div>
            <div className={styles.wrapper_choosingTimeAndRoute}>
              <div className={styles.wrapper_choosingTime}>
                <span className={styles.choosingTime_title}>
                  Желаемая дата и время заказа
                </span>
                <DatePicker
                  className={styles.choosingTime}
                  locale={locale}
                  onChange={setChoosingTime}
                  showTime={true}
                  format={DATE_FORMAT.DATE_WITH_TIME}
                  placeholder="Выберите дату и время"
                  dropdownClassName="no-now-btn"
                  disabledDate={getDisabledDate}
                  getPopupContainer={trigger => trigger.parentNode as HTMLElement}
                />
              </div>
              <div className={styles.wrapper_rout}>
                <RouteMap addresses={request.expected.waypoints} />
              </div>
            </div>
            <div className={styles.wrapper_additionalInformation}>
              <div className={styles.additionalInformation}>
                <span className={styles.additionalInformation_title}>Информация о поездке</span>
                <div className={styles.additionalInformation_img} onClick={() => setVisibleInformationTrip(!visibleInformationTrip)}>
                  <img
                    alt="Up"
                    src={Up}
                    className={cn(visibleInformationTrip ? styles.img_up : styles.img_down)}
                  />
                </div>
              </div>
              {visibleInformationTrip && (
              <div className={styles.wrapperinfoTrip}>
                <div className={styles.tripTitleItemsWrapper}>
                  {passengerCount
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Количество пассажиров</span>
                    <span className={styles.textInfo}>{declOfNum(passengerCount)}</span>
                  </div>
                  )}
                  {passengerName
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>ФИО клиента</span>
                    <span className={styles.textInfo}>{passengerName}</span>
                  </div>
                  )}
                  {passengerMobilePhone
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Телефон клиента</span>
                    <span className={styles.textInfo}>{parseNumber(passengerMobilePhone)}</span>
                  </div>
                  )}
                  {clientFullName
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>ФИО доп. контактного лица</span>
                    <span className={styles.textInfo}>{clientFullName}</span>
                  </div>
                  )}
                  {clientInfoPhone
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Телефон доп. контактного лица</span>
                    <span className={styles.textInfo}>{parseNumber(clientInfoPhone)}</span>
                  </div>
                  )}
                  {numberFlight
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Номер рейса/поезда</span>
                    <span className={styles.textInfo}>{numberFlight}</span>
                  </div>
                  )}
                  {dateFlight
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Дата и время рейса/поезда</span>
                    <span className={styles.textInfo}>{zoneTime(dateFlight, DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME, request?.timeZone)}</span>
                  </div>
                  )}
                  {phoneHotel
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Номер в гостинице</span>
                    <span className={styles.textInfo}>{phoneHotel}</span>
                  </div>
                  )}
                  {bugsComment
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Количество багажа</span>
                    <span className={styles.textInfo}>{bugsComment}</span>
                  </div>
                  )}
                </div>
              </div>
              )}
            </div>
            <div className={styles.wrapper_additionalInformation}>
              <div className={styles.additionalInformation}>
                <span className={styles.additionalInformation_title}>Дополнительные параметры</span>
                <div className={styles.additionalInformation_img} onClick={() => setVisibleAdditionalParameters(!visibleAdditionalParameters)}>
                  <img
                    alt="Up"
                    src={Up}
                    className={cn(visibleAdditionalParameters ? styles.img_up : styles.img_down)}
                  />
                </div>
              </div>
              {visibleAdditionalParameters && (
              <div className={styles.wrapperinfoTrip}>
                <div className={styles.tripTitleItemsWrapper}>
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Детское кресло</span>
                    {childSeatDetails && childSeatDetails.group1 > 0
                    && <span className={styles.textInfo}>{`Кресло от 9 мес. до 4 лет - ${childSeatDetails?.group1} шт.`}</span>}
                    {childSeatDetails && childSeatDetails.group2 > 0
                    && <span className={styles.textInfo}>{`Кресло 3-7 лет - ${childSeatDetails?.group2} шт.`}</span>}
                    {childSeatDetails && childSeatDetails.booster > 0
                    && <span className={styles.textInfo}>{`бустер 6-12 лет - ${childSeatDetails?.booster} шт.`}</span>}
                    {childSeatDetails && childSeatDetails.newborn > 0
                    && <span className={styles.textInfo}>{`люлька до 1 года - ${childSeatDetails?.newborn} шт.`}</span>}
                  </div>
                  {bugsOversizedComment
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Негабаритный багаж</span>
                    <span className={styles.textInfo}>{bugsOversizedComment}</span>
                  </div>
                  )}
                  {animalComment
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Перевозка животного</span>
                    <span className={styles.textInfo}>{animalComment}</span>
                  </div>
                  )}
                  {typeVehicle
                  && (
                  <div className={styles.tripTitleItemWrapper}>
                    <span className={styles.titleInformation}>Желаемый тип ТС</span>
                    <span className={styles.textInfo}>{typeVehicle}</span>
                  </div>
                  )}
                </div>
              </div>
              )}
            </div>
            <div className={styles.wrapper_additionalInformation}>
              <div className={styles.additionalInformation}>
                <span className={styles.additionalInformation_title}>Комментарий к цели поездки</span>
                <div className={styles.additionalInformation_img} onClick={() => setVisibleCommentPurpose(!visibleCommentPurpose)}>
                  <img
                    alt="Up"
                    src={Up}
                    className={cn(visibleCommentPurpose ? styles.img_up : styles.img_down)}
                  />
                </div>
              </div>
              {visibleCommentPurpose && (
              <span className={styles.commentForPurpose}>
                {request.commentForPurpose}
              </span>
              )}
            </div>
            <div className={styles.buttons}>
              <Button
                type="text"
                onClick={onCancel}
              >
                Отменить
              </Button>
              <Button
                type="primary"
                disabled={choosingTime ? false : true}
                onClick={reloadTrip}
              >
                Повторить
              </Button>
            </div>
          </div>
        </Modal>
      </div>
    );
  }
);
