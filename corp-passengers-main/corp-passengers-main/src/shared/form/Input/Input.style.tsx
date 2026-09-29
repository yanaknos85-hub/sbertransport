/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';
import { Input } from 'antd';

import { input } from '../styles';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const StyledInput = styled(Input)<any>`
  ${input}
`;
