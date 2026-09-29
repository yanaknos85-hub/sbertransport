/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import React, { FC } from 'react';
import styled, { css } from 'styled-components';

const Cell = styled('div')<any>`
  box-sizing: border-box;
  border: 1px solid rgba(38, 38, 38, 0.08);
  border-radius: 6px;
  padding: 6px 8px;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
  letter-spacing: -0.154px;
  min-width: 38px;

  ${props => props.active
  && css`
      border: 1px solid #10bf6a;
    `}
  cursor: ${props => (props.isPeriod ? 'not-allowed' : 'pointer')}
`;

const Component: FC<{ active: boolean; isPeriod?: boolean }> = ({
  children, active, isPeriod,
}) => (
  <Cell active={active} isPeriod={isPeriod}>
    {children}
  </Cell>
);

export default Component;
