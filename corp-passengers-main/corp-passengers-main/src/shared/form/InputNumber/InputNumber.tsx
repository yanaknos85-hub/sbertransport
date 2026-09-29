import React, { FC } from 'react';
import { InputNumberProps } from 'antd/lib/input-number';

import { StyledInput } from './InputNumber.style';

const InputNumber: FC<InputNumberProps> = props => <StyledInput {...props} />;

export default InputNumber;
