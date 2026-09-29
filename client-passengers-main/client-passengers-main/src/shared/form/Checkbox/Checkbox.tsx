import { CheckboxProps } from 'antd/lib/checkbox';
import React, { FC } from 'react';

import { StyledCheckbox } from './Checkbox.style';

const Checkbox: FC<CheckboxProps> = ({
  value, children, ...rest
}) => (
  <StyledCheckbox {...rest}>{children}</StyledCheckbox>
);

export default Checkbox;
