/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled, { css } from 'styled-components';

export const Field = styled('div')<any>`
  ${props => props.checkboxed
  && css`
      label {
        font-family: SB Sans Text;
        font-size: 14px;
        line-height: 22px;
        letter-spacing: -0.3px;
        color: #262626;
      }
    `}
`;

export const Label = styled('label')<any>`
  display: block;
  font-family: SB Sans Interface;
  font-size: 12px !important;
  line-height: 16px;
  letter-spacing: 0.2px;
  color: #262626;
  margin-bottom: 8px;
`;

export const Content = styled('div')<any>``;

export const Error = styled('div')<any>``;
