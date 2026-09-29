/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

import { input, inputFocus } from '../styles';

export const StyledMaskedInput = styled('div')<any>`
  .ant-input {
    ${input}
  }

  .ant-input:focus {
    ${inputFocus}
  }
`;
