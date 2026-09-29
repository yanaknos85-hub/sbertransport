import React, { FC } from 'react';
import {
  DatePicker, Form, FormInstance, Select
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import moment from 'moment';
import { ValidationRules } from 'shared/fieldValidationRules';
import { formatRubles } from 'utils';

import { DATE_FORMAT } from 'constants/constants.app';

import { getDisabledDate } from '../../../../../../../EditTripRequestForm/components/TripRequestDate';
import { getFieldValue } from '../../../additional/utils';
import { costTravelCard, fieldTitles, publicTransportTypeTitle } from '../../../constants';

const { RangePicker } = DatePicker;

export interface Props {
  form: FormInstance;
  field: FormListFieldData;
  handleTransportTypeChange: (selectedTransportType: SelectValue, field: FormListFieldData) => void;
  currentTransportTypes: LabeledValue[];
  spareDate: any;
}

export const TravelCardFormItem: FC<Props> = observer(
  ({
    form, field, handleTransportTypeChange, currentTransportTypes, spareDate,
  }) => (
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
          onChange={(transportType: any): void => handleTransportTypeChange(transportType, field)}
          placeholder={publicTransportTypeTitle}
          options={currentTransportTypes}
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
        <span>{formatRubles(getFieldValue(form, field, fieldTitles.cost) ?? 0)}</span>
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
          defaultValue={[moment(new Date(), DATE_FORMAT.MONTH_AND_YEAR), moment(spareDate, DATE_FORMAT.MONTH_AND_YEAR)]}
          picker="month"
          format={DATE_FORMAT.MONTH_AND_YEAR}
        />
      </Form.Item>
    </>
  )
);
