/* eslint-disable react/destructuring-assignment */
import React, { FC } from 'react';
import { MaskedInput as AndtMaskedInput } from 'libs/MaskedInput';

import { StyledMaskedInput } from './MaskedInput.style';

const MaskedInput: FC<any> = ({
  mask, value = '', onChange, minLength = 0,
}) => (
  <StyledMaskedInput>
    <AndtMaskedInput
      value={value.replace(/-|_|\)|\(/g, '')}
      mask={mask}
      onChange={onChange}
      minLength={minLength}
    />
  </StyledMaskedInput>
);

export default MaskedInput;
