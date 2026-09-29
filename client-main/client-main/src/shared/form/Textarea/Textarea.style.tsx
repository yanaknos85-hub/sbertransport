/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Input } from 'antd';
import styled from 'styled-components';

import { input } from '../styles';

export const StyledTextarea = styled(Input.TextArea)<any>`
  ${input}
`;
