import React, { ChangeEvent, FC } from 'react';
import { Form } from 'antd';
import { observer } from 'mobx-react';
import { CargoMainInnerContent } from 'shared/components/Cargo/CargoLayout';

import { StoreNames } from '../../../../ioc/ioc.storeNames';
import { ValidationRules } from '../../../fieldValidationRules';
import { FieldType } from '../../../form/Field/Field';
import FormField from '../../../form/FormField/FormField';
import { useAppStoreContext } from '../../../hooks/useEmpContext';

const CargoComment: FC = observer(() => {
  const { [StoreNames.cargoStore]: cargoStore } = useAppStoreContext();

  const [form] = Form.useForm();

  const field = {
    comment: {
      label: 'Комментарий к заказу',
      name: 'comment',
      type: FieldType.textarea,
      rules: [ValidationRules.general.maxLength(500)],
      params: {
        maxLength: 500,
        rows: 3,
        onChange: (e: ChangeEvent<HTMLInputElement>) => {
          cargoStore.setComment(e.target.value);
        },
      },
    },
  };

  return (
    <CargoMainInnerContent>
      <Form
        form={form}
        name="tariffForm"
        initialValues={{
          comment: cargoStore.comment,
        }}
      >
        <FormField
          {...field.comment}
        />
      </Form>
    </CargoMainInnerContent>
  );
});

export default CargoComment;
