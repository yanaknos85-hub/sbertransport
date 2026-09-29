import '../../../../../styles/override.css';
import { Form, FormInstance, Select } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue, SelectValue } from 'antd/es/select';
import React, { Dispatch, FC, SetStateAction } from 'react';
import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';

import { ValidationRules } from 'shared/fieldValidationRules';
import { PublicCityType, PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';

import {
  fieldTitles,
  minTicketQuantity,
  publicTransportTypeTitle,
  tripsInfoTitle
} from '../../../constants';

interface Props {
  form: FormInstance;
  field: FormListFieldData;
  tripCost: number;
  sum: number;
  updateSum: (index: number, quantity: string | number | null, price: number, typeChange: string) => void;
  handleTransportTypeChange: (selectedTransportType: SelectValue, index: number) => void;
  currentTransportTypes: LabeledValue[];
  index: number;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
}
export const CityFormItem: FC<Props> = ({
  form,
  field,
  tripCost,
  updateSum,
  handleTransportTypeChange,
  currentTransportTypes,
  index,
  setIsSaveCompensation,
}) => {
  const getCityTripCompensationFields = (): JSX.Element => (
    <div className="wrapper_compensation">
      <span className="compensation_title_item">{publicTransportTypeTitle}</span>
      <Form.Item
        {...field}
        key={`index-${fieldTitles.publicTransportType}`}
        name={[field.name, fieldTitles.publicTransportType]}
        rules={[ValidationRules.general.required]}
      >
        <Select
          suffixIcon={(
            <div style={{ padding: '5px 18px' }}>
              <ChevronSmall />
            </div>
          )}
          placeholder={publicTransportTypeTitle}
          onChange={(transportType: PublicCityType): void => {
            setIsSaveCompensation(false);
            updateSum(index, minTicketQuantity, tripCost, '');
            handleTransportTypeChange(transportType, index);
            form.setFieldsValue({
              tripsInfo: form
                .getFieldValue(tripsInfoTitle)
                .map((el: PublicInfoCard) => el.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION ? { ...el, ticketCount: 1 } : el
                ),
            });
          }}
          options={currentTransportTypes}
        />
      </Form.Item>
    </div>
  );

  return <>{getCityTripCompensationFields()}</>;
};
