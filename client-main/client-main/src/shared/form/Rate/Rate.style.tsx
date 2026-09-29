/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Rate } from 'antd';
import styled from 'styled-components';

import { rate } from '../styles';

export const StyledRate = styled(Rate)<any>`
  ${rate}
`;
