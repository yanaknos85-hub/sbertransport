// copy from https://github.com/antoniopresto/antd-mask-input
import Input, { InputProps } from 'antd/lib/input';
import IMask from 'imask';
import React, { useState } from 'react';

export interface MaskedInputProps extends Omit<InputProps, 'onChange' | 'value' | 'defaultValue'> {
  mask: MaskType;
  definitions?: InputMaskOptions['definitions'];
  value?: string;
  defaultValue?: string;
  maskOptions?: InputMaskOptions;
  onChange?: (event: string) => void;
}

export { IMask };

export const MaskedInput: React.FC<MaskedInputProps> = ({ onChange, value }, props) => {
  const formatPhoneNumber = (inputPhone: string) => {
    const cleaned = ('' + inputPhone).replace(/[^\d]/g, '');
    let defaultNumber = cleaned;

    if (cleaned.substring(0, 1) !== '7') {
      defaultNumber = '7' + defaultNumber;
    }

    const match = defaultNumber.match(/^(\d{1})(\d{0,3})(\d{0,3})(\d{0,2})(\d{0,2})$/);

    if (match) {
      let formatted = `+${match[1]}`;

      if (match[2]) formatted += ` ${match[3] ? '(' : ''}${match[2]}${match[3] ? ')' : ''}`;

      if (match[3]) formatted += ` ${match[3]}`;

      if (match[4]) formatted += ` ${match[4]}`;

      if (match[5]) formatted += ` ${match[5]}`;

      return formatted;
    }

    return defaultNumber;
  };

  const [phone, setPhone] = useState(formatPhoneNumber(value));

  const handlePhoneChange = (e: OnChangeEvent) => {
    const inputPhone = e.target.value;
    const formattedPhone = formatPhoneNumber(inputPhone);

    setPhone(formattedPhone);
    onChange && onChange(e.target.value);
  };

  return (
    <Input
      {...props}
      value={phone}
      onChange={handlePhoneChange}
      placeholder="+7 (___) ___ __ __"
      maxLength={18}
    />
  );
};

export default MaskedInput;

export type UnionToIntersection<T> = (T extends any ? (x: T) => any : never) extends (x: infer R) => any
  ? {
    [K in keyof R]: R[K];
  }
  : never;

type OnChangeParam = Parameters<Exclude<InputProps['onChange'], undefined>>[0];

interface OnChangeEvent extends OnChangeParam {
  maskedValue: string;
  unmaskedValue: string;
}

interface IMaskOptionsBase extends UnionToIntersection<IMask.AnyMaskedOptions> { }

export type InputMaskOptions = {
  [K in keyof IMaskOptionsBase]?: IMaskOptionsBase[K];
};

type MaskFieldType = string | RegExp | Function | Date | InputMaskOptions;

interface IMaskOptions extends Omit<InputMaskOptions, 'mask'> {
  mask: MaskFieldType;
}

interface MaskOptionsList extends Array<IMaskOptions> { }

export type MaskType = MaskFieldType | MaskOptionsList;
