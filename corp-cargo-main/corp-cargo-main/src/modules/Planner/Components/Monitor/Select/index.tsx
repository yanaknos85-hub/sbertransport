import React from 'react';
import { SelectStyled } from './select.styles';
import DownArrowIcon from './assets/DownArrowIcon';

const Select = ({ ...props }) => <SelectStyled suffixIcon={DownArrowIcon} {...props} />;
export default Select;
