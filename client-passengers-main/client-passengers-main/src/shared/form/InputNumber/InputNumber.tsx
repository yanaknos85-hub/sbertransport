import React, { FC } from 'react';

import { StyledInput } from './InputNumber.style';

interface DefaultInputProps {
  placeholderText?: string;
}

const InputNumber: FC<DefaultInputProps> = props => {
  const { placeholderText } = props;

  return (<StyledInput placeholder={placeholderText ? placeholderText : ''} {...props} />);
};

export default InputNumber;
