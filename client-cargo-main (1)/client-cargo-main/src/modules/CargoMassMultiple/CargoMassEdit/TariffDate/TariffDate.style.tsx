/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const List = styled('div')`
  display: flex;
`;

export const Item = styled('div')`
  width: 50%;
  box-sizing: border-box;

  &:first-child {
    padding-right: calc(76px / 2);
  }

  &:first-child + div {
    padding-left: calc(76px / 2);
  }
`;

export const Params = styled('div')`
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
`;

export const Param = styled('div')`
  margin-right: 15px;
  min-width: 64px;
`;

export const Label = styled('div')<{ price?: boolean }>`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 12px;
  line-height: 16px;
  letter-spacing: 0.2px;
  color: #909090;
`;

export const Value = styled('div')<{ price?: boolean }>`
  font-family: "SB Sans Text", serif, sans-serif;
  color: #262626;
  margin-top: 16px;
  font-size: ${props => (props.price ? 24 : 16)}px;
  font-weight: ${props => (props.price ? 600 : 'normal')};
  line-height: 16px;
`;
