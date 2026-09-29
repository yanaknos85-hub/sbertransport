import React, { FC } from 'react';
import { InputProps } from 'antd/lib/input';

import { StyledInput } from './Input.style';

const Input: FC<InputProps> = props => <StyledInput {...props} />;

export default Input;
