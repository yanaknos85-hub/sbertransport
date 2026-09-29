/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

import { ReactComponent as DropdownIconSvg } from './images/arrow.svg';
import { ReactComponent as SelectedIconSvg } from './images/arrowSelected.svg';

export const Sorter = styled.div`
  display: flex;
  align-items: center;
  position: relative;
  user-select: none;
`;

export const Selected = styled.div`
  cursor: pointer;
  display: flex;
  align-items: center;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 12px;
  line-height: 20px;
`;

export const Dropdown = styled('div')<{ open: boolean }>`
  display: ${props => (props.open ? 'block' : 'none')};
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 0;
  padding: 19px 0;
  width: 256px;
  background: #ffffff;
  box-shadow: 0 0 1px rgba(0, 0, 0, 0.12), 0 12px 24px -6px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
`;

export const DropdownSelected = styled.div`
  padding: 0 16px 13px 16px;
  font-weight: 300;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 14px;
  line-height: 22px;
  color: #10bf6a;
  display: flex;
  justify-content: space-between;
`;

export const SelectedIcon = styled(SelectedIconSvg)``;

export const DropdownItem = styled('div')`
  padding: 16px 0;
  border-top: 1px solid rgba(38, 38, 38, 0.08);
`;

export const DropdownIcon = styled(DropdownIconSvg)<{ down: string }>`
  margin-left: 6px;
  transform: rotate(${props => (props.down === 'true' ? 180 : 0)}deg);
`;

export const ListLabel = styled.div`
  display: flex;
  align-items: center;
  padding: 0 16px;
`;

export const ListLabelIcon = styled('div')`
  margin-right: 12px;
  height: 24px;
  width: 24px;
`;

export const ListLabelText = styled.div`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 10px;
  line-height: 12px;
  letter-spacing: 0.2px;
  color: #909090;
  text-transform: uppercase;
`;

export const List = styled.div``;

export const ListItem = styled('div')<{ top?: number }>`
  padding: 0 16px;
  font-family: 'SB Sans Text', serif, sans-serif;
  letter-spacing: -0.3px;
  font-weight: 300;
  font-size: 14px;
  line-height: 22px;
  color: #262626;
  margin-top: ${props => props.top ?? 14}px;
  cursor: pointer;

  &:hover {
    color: #909090;
  }
`;
