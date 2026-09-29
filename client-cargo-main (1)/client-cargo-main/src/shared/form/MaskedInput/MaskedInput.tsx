/* eslint-disable react/destructuring-assignment */
import React, { FC } from 'react';
import AndtMaskedInput from 'libs/MaskedInput';

import { StyledMaskedInput } from './MaskedInput.style';

const MaskedInput: FC<any> = ({
  mask, value = '', onChange, minLength = 0,
}) => (
  <StyledMaskedInput>
    <AndtMaskedInput
      value={value.replace(/-|_|\)|\(/g, '')}
      mask={mask}
      onChange={e => onChange(e.target.value)}
      minLength={minLength}
    />
  </StyledMaskedInput>
);

export default MaskedInput;
