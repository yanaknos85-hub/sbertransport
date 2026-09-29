import '../../override.scss';
import {
  DatePicker, Divider, Form, Input
} from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import infoTripStyles from './infoTrip.module.scss';
import {
  fieldTitles, flightDate, flightNumber, hotelPhoneNumber
} from '../../constants';
import { DATE_FORMAT } from 'constants/constants.app';
import { getDisabledDate } from 'modules/EmployeeApp/components/EditTripRequestForm/components/TripRequestDate';

export const InfoTrip: FC = observer(
  (): JSX.Element => {
    return (
      <div className="wrapper_groupTransfer">
        <span className="groupTransfer_title_item">{flightNumber}</span>
        <Form.Item
          key={fieldTitles.numberFlight}
          name={fieldTitles.numberFlight}
        >
          <Input placeholder="Введите номер рейса или поезда" />
        </Form.Item>
        <span className="groupTransfer_title_item">{flightDate}</span>
        <Form.Item
          key={fieldTitles.dateFlight}
          name={fieldTitles.dateFlight}
        >
          <DatePicker
            placeholder="Укажите дату и время"
            showTime={true}
            format={DATE_FORMAT.DATE_WITH_TIME}
            dropdownClassName="no-now-btn"
            disabledDate={getDisabledDate}
            locale={locale}
          />
        </Form.Item>
        <span className="groupTransfer_title_item">{hotelPhoneNumber}</span>
        <Form.Item
          key={fieldTitles.phoneHotel}
          name={fieldTitles.phoneHotel}
        >
          <Input className={infoTripStyles.telephone_title} placeholder="Укажите номер" />
        </Form.Item>
        <Divider />
      </div>
    );
  }
);
