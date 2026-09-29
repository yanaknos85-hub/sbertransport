import React, { FC } from 'react';
import { SelectProps, SelectValue } from 'antd/lib/select';

import { ReactComponent as Icon } from './images/selectArrow.svg';
import { StyledSelectMultiple } from './SelectMultiple.style';

const SelectMultiple: FC<SelectProps<SelectValue>> = props => (
  <StyledSelectMultiple
    className="ant-selectMultiple-customize-input"
    suffixIcon={<Icon />}
    {...props}
  />
);

export default SelectMultiple;
