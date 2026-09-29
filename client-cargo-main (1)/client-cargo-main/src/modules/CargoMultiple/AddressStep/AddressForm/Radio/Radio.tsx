import React, { FC } from 'react';
import type { RadioChangeEvent } from 'antd';
import { Form, Radio as RadioAnt } from 'antd';

import { AddressLoadType } from 'stores/Cargos/typesMulti';

import * as S from './Radio.styles';

interface Props {
  value: string;
  name: number;
  onChange: (e: RadioChangeEvent) => void;
}

export const Radio: FC<Props> = props => {
  const { value, onChange } = props;

  return (
    <Form.Item>
      <RadioAnt.Group
        value={value}
        onChange={onChange}
      >
        <S.Radio
          value={AddressLoadType.LOAD}
          checked
        >
          Сбор
        </S.Radio>
        <S.Radio name={name} value={AddressLoadType.UNLOAD}>Доставка</S.Radio>
      </RadioAnt.Group>
    </Form.Item>
  );
};

