/* eslint-disable @stylistic/max-statements-per-line */
import React, { FC } from 'react';

import { StyledInput } from './Input.style';

interface DefaultInputProps {
  upperCase?: boolean;
  placeholderText?: string;
}

const Input: FC<DefaultInputProps> = props => {
  const { placeholderText, upperCase } = props;

  return (
    <StyledInput
      placeholder={placeholderText ? placeholderText : ''}
      onInput={(e: { target: { value: string } }) => { if (upperCase) { return e.target.value = e.target.value.toUpperCase(); } }}
      {...props}
    />
  );
};

export default Input;
