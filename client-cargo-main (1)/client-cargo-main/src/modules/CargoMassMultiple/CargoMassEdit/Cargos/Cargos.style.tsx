/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled, { css } from 'styled-components';

import { ReactComponent as DeleteIcon } from './images/delete.svg';

export const Divider = styled('div')`
  width: 100%;
  height: 1px;
  background: rgba(38, 38, 38, 0.08);
`;

export const List = styled('div')`
  display: flex;
  flex-wrap: wrap;
  margin: 0 -8px -16px -8px;
`;

export const Item = styled('div')`
  background: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08), 0 0 1px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  letter-spacing: -0.3px;
  color: #262626;
  margin: 0 8px 16px 8px;
  width: calc(100% / 3 - 8px * 2);
`;

export const Inner = styled('div')<{ top?: boolean; bottom?: boolean }>`
  padding: 16px;

  ${props => props.top
  && css`
      padding-bottom: 12px;
    `};

  ${props => props.bottom
  && css`
      padding-top: 12px;
    `};
`;

export const Name = styled('div')`
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 14px;
  line-height: 22px;
  margin-bottom: 4px;
  position: relative;
`;

export const Delete = styled(DeleteIcon)<{ len: number }>`
  position: absolute;
  top: 0;
  right: 0;
  cursor: pointer;
  pointer-events: ${({ len }) => len > 1 ? 'auto' : 'none'};
`;

export const Caption = styled('div')`
  display: flex;
  align-items: center;
`;

export const Type = styled('div')`
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 12px;
  line-height: 20px;
  color: #909090;
  margin-left: 12px;
`;

export const Params = styled('div')`
  display: flex;
`;

export const Param = styled('div')`
  width: calc(100% / 3);
  :not(:last-child) {
    margin-right: 4px;
  }
`;

export const ParamLabel = styled('div')`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 12px;
  line-height: 16px;
  letter-spacing: 0.2px;
  color: #909090;
  margin-bottom: 2px;
`;

export const ParamValue = styled('div')`
  font-family: 'SB Sans Text', serif;, sans-serif;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: #262626;
`;
