import {
  Divider, Form, FormInstance, InputNumber, Select
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import React, { FC, useEffect, useState } from 'react';

import { ValidationRules } from 'shared/fieldValidationRules';
import { PublicCityType, PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { formatRubles } from 'utils';

import { getFieldValue } from '../../../additional/utils';
import {
  costOneTrip,
  fieldTitles,
  minTicketQuantity,
  publicTransportTypeTitle,
  sumLabel,
  ticketCountLabel,
  tripsInfoTitle
} from '../../../constants';

import styles from '../../../style.module.scss';

interface Props {
  form: FormInstance;
  field: FormListFieldData;
  tripCost: number;
  sum: number;
  updateSum: (field: FormListFieldData, quantity: string | number | null, price: number) => void;
  handleTransportTypeChange: (selectedTransportType: SelectValue, field: FormListFieldData) => void;
  currentTransportTypes: LabeledValue[];
}
export const CityFormItem: FC<Props> = ({
  form,
  field,
  tripCost,
  sum,
  updateSum,
  handleTransportTypeChange,
  currentTransportTypes,
}) => {
  const textInput = React.useRef(null);
  const [cost, setCost] = useState<number | undefined>(0);

  useEffect(() => {
    setCost(getFieldValue(form, field, fieldTitles.sum));
  }, []);

  const getCityTripCompensationFields = (): JSX.Element => (
    <>
      <Form.Item
        {...field}
        key={`index-${fieldTitles.publicTransportType}`}
        name={[field.name, fieldTitles.publicTransportType]}
        // @ts-ignore
        fieldKey={[field.fieldKey, fieldTitles.publicTransportType]}
        label={publicTransportTypeTitle}
        rules={[ValidationRules.general.required]}
      >
        <Select
          placeholder={publicTransportTypeTitle}
          onChange={(transportType: PublicCityType): void => {
            updateSum(field, minTicketQuantity, tripCost);
            handleTransportTypeChange(transportType, field);
            form.setFieldsValue({
              tripsInfo: form
                .getFieldValue(tripsInfoTitle)
                .map((el: PublicInfoCard) => el.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION ? { ...el, ticketCount: 1 } : el
                ),
            });
          }}
          // FIXME @typescript-eslint/explicit-function-return-type
          options={currentTransportTypes}
        />
      </Form.Item>
      <Form.Item
        {...field}
        key={fieldTitles.cost}
        name={[field.name, fieldTitles.cost]}
        // @ts-ignore
        fieldKey={[field.fieldKey, fieldTitles.cost]}
        label={costOneTrip}
      >
        <span>{formatRubles(tripCost / 100) ?? 0}</span>
      </Form.Item>
      <Form.Item
        {...field}
        key={fieldTitles.ticketCount}
        name={[field.name, fieldTitles.ticketCount]}
        // @ts-ignore
        fieldKey={[field.fieldKey, fieldTitles.ticketCount]}
        label={ticketCountLabel}
        rules={[ValidationRules.general.required]}
        initialValue={minTicketQuantity}
      >
        <InputNumber
          min={minTicketQuantity}
          max={999}
          ref={textInput}
          type="number"
          onChange={(quantity = minTicketQuantity): void => updateSum(field, quantity, tripCost)}
          // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
          formatter={value => Number(value).toFixed()}
          // FIXME @typescript-eslint/explicit-function-return-type
          className={styles.fullScreen}
        />
      </Form.Item>
      <Form.Item
        {...field}
        key={fieldTitles.sum}
        name={[field.name, fieldTitles.sum]}
        // @ts-ignore
        fieldKey={[field.fieldKey, fieldTitles.sum]}
        label={sumLabel}
      >
        <span>{formatRubles(cost ? cost / 100 : sum / 100)}</span>
      </Form.Item>
      <Divider />
    </>
  );

  return <>{getCityTripCompensationFields()}</>;
};
