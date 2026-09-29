import React from 'react';

import DownArrowIcon from './assets/DownArrowIcon';
import { SelectStyled } from './select.styles';

const Select = ({ ...props }) => <SelectStyled suffixIcon={DownArrowIcon} {...props} />;
export default Select;
