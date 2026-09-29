import React, { FC } from 'react';
import { SelectProps, SelectValue } from 'antd/lib/select';

import { ReactComponent as Icon } from './images/selectArrow.svg';
import { StyledSelect } from './Select.style';

const Select: FC<SelectProps<SelectValue>> = props => (
  <StyledSelect
    className="ant-select-customize-input"
    suffixIcon={<Icon />}
    {...props}
  />
);

export default Select;
