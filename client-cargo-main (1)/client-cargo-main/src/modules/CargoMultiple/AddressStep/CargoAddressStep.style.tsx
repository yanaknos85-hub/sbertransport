/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Switch as SwitchAnt } from 'antd';
import styled from 'styled-components';

export const Section = styled('div')`

  &:first-child {
    border-bottom: 1px solid rgba(38, 38, 38, 0.08);
  }

  &:first-child + & {
    margin-top: 24px;
`;

export const Header = styled('div')`
  display: flex;
  justify-content: space-between;
`;

export const ControlsContainer = styled('div')`
  display: flex;
  flex-direction: column;
`;

export const Controls = styled('div')`
  margin-top: 16px;
  color: #262626;
  
  &:first-child {
    margin-top: 24px;
  }
`;

export const Title = styled('div')`
  font-size: 24px;
  font-weight: 600;
`;

export const Button = styled('button')`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px 16px;
  color: var(--gray-10);
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid var(--gray-10);
  font-family: 'SB Sans Text', sans-serif, serif;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-align: center;
`;

export const Switch = styled(SwitchAnt)`
  margin-right: 12px;
  color: #262626;
`;

export const SwitchTitle = styled('span')`
  font-size: 14px;
  margin-left: 12px;
`;

export const Img = styled('img')`
  margin-left: 8px;
`;
