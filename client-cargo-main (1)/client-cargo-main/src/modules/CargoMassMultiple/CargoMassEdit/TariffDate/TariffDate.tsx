/* eslint-disable react/destructuring-assignment */
import React, { FC, useEffect } from 'react';
import { FormInstance } from 'antd/es/form/Form';
import { observer } from 'mobx-react';
import moment from 'moment';
import { emptySign } from 'shared/constants/constants';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';

import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import { DeliveryUrgencyEnum } from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';
import * as utils from 'utils/Misc';
import { TariffsOption } from 'modules/CargoMassMultiple/types';

import { buildDateRegular } from '../../utils';
import {
  Item, Label, List, Param, Params, Value
} from './TariffDate.style';

interface Props {
  form: FormInstance;
  date: number;
  calculateTariffs: (tripDate: number) => void;
  tariff: TariffCost | null;
  express: boolean;
  isRegular: boolean;
  tariffsOptions: TariffsOption[];
}

const TariffDateMultiple: FC<Props> = observer(({
  form,
  date,
  calculateTariffs,
  tariff,
  express,
  isRegular,
  tariffsOptions,
}) => {
  const { required } = ValidationRules.general;
  const deliveryTime = tariff && tariff.deliveryTime ? `${tariff.deliveryTime} ${utils.declOfNum(tariff.deliveryTime, ['день', 'дня', 'дней'])}` : emptySign;

  const disabledDate = (current: moment.Moment): boolean => {
    const now = moment().add(0, 'days');
    return current < now || current > now.endOf('year');
  };

  const data = isRegular ? {
    format: buildDateRegular,
    showToday: false,
    disabledDate,
  } : {
    format: (ms: number): string => moment(ms).format(DATE_FORMAT.DATE_WITH_TIME_DOTS),
    showTime: { format: DATE_FORMAT.TIME_FULL },
    showNow: false,
    disabledDate,
  };

  const field = {
    tariff: {
      type: FieldType.select,
      label: 'Тариф',
      name: 'tariff',
      params: {
        options: tariffsOptions,
      },
    },
    desiredDate: {
      type: FieldType.date,
      label: 'Дата отправления',
      name: 'desiredDate',
      initialValue: moment(date),
      params: {
        ...data,
        onOk: (val: string) => {
          calculateTariffs(moment(val).utc().valueOf());
        },
      },
      rules: [required],
    },
    deliveryUrgency: {
      label: 'Срочность',
      name: 'deliveryUrgency',
      type: FieldType.radio,
      rules: [ValidationRules.general.required],
      initialValue: express ? DeliveryUrgencyEnum.express : DeliveryUrgencyEnum.standart,
      params: {
        options: [
          { label: 'Стандарт', value: DeliveryUrgencyEnum.standart },
          { label: 'Экспресс', value: DeliveryUrgencyEnum.express },
        ],
      },
    },
  };

  useEffect(() => {
    form.setFieldsValue({ tariff: tariff?.transportType.nameRus });
  }, [tariff]);

  return (
    <>
      <List>
        <Item>
          <FormField {...field.tariff} />
        </Item>
        <Item>
          <FormField {...field.desiredDate} />
        </Item>
      </List>
      {/* Cкрыто в рамках задачи TRANSPORT-11155
      <List>
        <Item>
          <FormField {...field.deliveryUrgency} />
        </Item>
      </List> */}
      <Params>
        <Param>
          <Label>Срок</Label>
          <Value>{deliveryTime || emptySign}</Value>
        </Param>
        <Param>
          <Label price={true}>Стоимость</Label>
          <Value
            price={true}
            dangerouslySetInnerHTML={{ __html: tariff?.cost ? `${tariff.cost / 100} &#8381;` : emptySign }}
          />
        </Param>
      </Params>
    </>
  );
});

export default TariffDateMultiple;
