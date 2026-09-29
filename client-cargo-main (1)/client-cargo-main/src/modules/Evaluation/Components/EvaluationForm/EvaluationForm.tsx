import React, { FC } from 'react';
import { FormInstance } from 'antd/es/form/Form';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import { Addresses } from '../Addresses/Addresses';
import { HeaderComponent } from '../Header/HeaderComponent';
import { Rating } from '../Rating/Rating';

interface Props {
  title: string;
  subTitle: string;
  request: CargoRequestModel;
  onClickDetailedHandler: (id: string) => void;
  form: FormInstance;
  onSubmit: (values: any) => void;
  disabling: boolean;
  setDisabling: (value: boolean) => void;
}

export const EvaluationForm: FC<Props> = ({
  title,
  subTitle,
  request,
  onClickDetailedHandler,
  form,
  onSubmit,
  disabling,
  setDisabling,
}) => (
  <>
    <HeaderComponent title={title} subTitle={subTitle} />
    <Addresses request={request} onClickDetailedHandler={onClickDetailedHandler} />
    <Rating
      form={form}
      onSubmit={onSubmit}
      disabling={disabling}
      setDisabling={setDisabling}
      request={request}
    />
  </>
);
