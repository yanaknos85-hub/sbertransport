import { InputProps } from 'antd/lib/input';
import React, { FC } from 'react';

import { StyledInput } from './Input.style';

const Input: FC<InputProps> = props => <StyledInput {...props} />;

export default Input;
