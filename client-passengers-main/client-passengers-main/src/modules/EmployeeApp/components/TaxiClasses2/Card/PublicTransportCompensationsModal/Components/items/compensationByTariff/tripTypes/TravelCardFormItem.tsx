/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  DatePicker, Form, FormInstance, Select
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { FC } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import { ValidationRules } from 'shared/fieldValidationRules';
import { formatRubles } from 'utils';

import { getDisabledDate } from '../../../../../../../EditTripRequestForm/components/TripRequestDate';
import {
  costTravelCard,
  fieldTitles,
  minTicketQuantity,
  publicTransportTypeTitle,
  tripsInfoTitle
} from '../../../constants';
import { PublicCityType, PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';

const { RangePicker } = DatePicker;

export interface Props {
  form: FormInstance;
  field: FormListFieldData;
  handleTransportTypeChange: (selectedTransportType: SelectValue, field: FormListFieldData) => void;
  currentTransportTypes: LabeledValue[];
  spareDate: any;
  updateSum: (field: FormListFieldData, quantity: string | number | null, price: number) => void;
  tripCost: number;
}

export const TravelCardFormItem: FC<Props> = observer(
  ({
    form, field, handleTransportTypeChange, currentTransportTypes, spareDate, updateSum, tripCost,
  }) => {
    const changePublicTransportType = (transportType: PublicCityType) => {
      updateSum(field, minTicketQuantity, tripCost);
      handleTransportTypeChange(transportType, field);
      form.setFieldsValue({
        tripsInfo: form
          .getFieldValue(tripsInfoTitle)
          .map((el: PublicInfoCard) => el.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION ? { ...el, ticketCount: 1 } : el
          ),
      });
    };

    return (
      <>
        <Form.Item
          {...field}
          key={fieldTitles.publicTransportType}
          name={[field.name, fieldTitles.publicTransportType]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.publicTransportType]}
          label={publicTransportTypeTitle}
          rules={[ValidationRules.general.required]}
        >
          <Select
            placeholder={publicTransportTypeTitle}
            options={currentTransportTypes}
            onChange={(transportType: PublicCityType): void => changePublicTransportType(transportType)}
          />
        </Form.Item>

        <Form.Item
          {...field}
          key={fieldTitles.cost}
          name={[field.name, fieldTitles.cost]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.cost]}
          label={costTravelCard}
        >
          <span>{formatRubles(tripCost / 100) ?? 0}</span>
        </Form.Item>

        <Form.Item
          {...field}
          key={fieldTitles.calendar}
          name={[field.name, fieldTitles.calendar]}
          // @ts-ignore
          fieldKey={[field.fieldKey, fieldTitles.calendar]}
          label="Период действия проездного"
          rules={[ValidationRules.general.required]}
          initialValue={[moment(new Date(), DATE_FORMAT.MONTH_AND_YEAR), moment(spareDate, DATE_FORMAT.MONTH_AND_YEAR)]}
        >
          <RangePicker
            disabledDate={getDisabledDate}
            defaultValue={[
              moment(new Date(), DATE_FORMAT.MONTH_AND_YEAR),
              moment(spareDate, DATE_FORMAT.MONTH_AND_YEAR),
            ]}
            picker="month"
            format={DATE_FORMAT.MONTH_AND_YEAR}
          />
        </Form.Item>
      </>
    );
  }
);
