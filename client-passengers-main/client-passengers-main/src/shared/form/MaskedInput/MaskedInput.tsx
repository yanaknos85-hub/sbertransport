/* eslint-disable react/destructuring-assignment */
import React, { FC } from 'react';
import AndtMaskedInput, { MaskType } from 'libs/MaskedInput';

import { StyledMaskedInput } from './MaskedInput.style';

export interface MaskedInputProps {
  mask: MaskType;
  value?: string;
  minLength?: number;
  onChange?: (event: string) => void;
}

const MaskedInput: FC<MaskedInputProps> = ({
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
