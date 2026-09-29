import React, { FunctionComponent, useState } from 'react';
import { Form, FormInstance } from 'antd';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import { MIDDLE_RATE } from '../../constants';
import { ButtonBlock, RateBlock, Subtitle } from '../../static/styles';
import { renderRateTitle } from '../../utils';
import { Icons } from '../Icons/Icons';

interface Props {
  comment?: string;
  form: FormInstance;
  onSubmit: (values: any) => void;
  disabling: boolean;
  setDisabling: (value: boolean) => void;
  request: CargoRequestModel;
}

export const Rating: FunctionComponent<Props> = ({
  comment, form, onSubmit, disabling, setDisabling, request,
}) => {
  const [isGreenStar, setIsGreenStar] = useState(false);

  const [rating, setRating] = useState(0);

  const { rateTitle, rateSubtitle } = renderRateTitle(form.getFieldValue('rating'));

  const onHoverChange = (e: number) => {
    setIsGreenStar(e > MIDDLE_RATE);
  };

  const handleRating = (value: number) => {
    setRating(value);

    setDisabling(value === 0);
  };

  return (
    <Form
      name="feedback-form"
      form={form}
      onFinish={onSubmit}
      initialValues={{ rating: 0 }}
    >
      <RateBlock>
        {rateTitle}
        <FormField
          name="rating"
          type={FieldType.rate}
          params={{
            onChange: (value: number) => handleRating(value),
            onHoverChange,
            value: 0,
            allowClear: true,
            isGreenStar,
            rate: form.getFieldValue('rating'),
            middleRate: MIDDLE_RATE,
          }}
        />
      </RateBlock>
      <Subtitle>{rateSubtitle}</Subtitle>
      {form.getFieldValue('rating') > 0 && (
        <>
          <Icons
            rating={rating}
            form={form}
            transportType={request.transportType}
          />
          <h3>Комментарий</h3>
          <FormField
            value={comment}
            name="comment"
            type={FieldType.textarea}
            rules={[ValidationRules.general.maxLength(180)]}
            params={{
              placeholder: 'Что бы Вы отметили?',
              maxLength: 180,
              rows: 4,
            }}
          />
        </>
      )}
      <ButtonBlock>
        <TButton
          disabled={disabling}
          htmlType="submit"
          $size="small"
          style={{
            backgroundColor: disabling ? 'rgba(16,191,106, 0.4)' : '',
            border: 'none',
            color: disabling ? 'white' : '',
          }}
        >
          Готово
        </TButton>
      </ButtonBlock>
    </Form>
  );
};
