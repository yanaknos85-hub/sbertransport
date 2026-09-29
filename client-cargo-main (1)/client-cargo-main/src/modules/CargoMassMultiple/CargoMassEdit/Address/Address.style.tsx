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

export const ItemLabel = styled('div')`
  display: flex;
  align-items: center;
  font-family: 'SB Sans Text', sans-serif, serif;
  font-size: 16px;
  line-height: 20px;
  letter-spacing: -0.32px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 16px;
`;

export const ItemWaypoint = styled('div')<{ color: string }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: ${props => (props.color === 'blue' ? '#6979F7' : '#19B150')};
  font-family: 'SB Sans Text', sans-serif, serif;
  font-size: 10px;
  line-height: 12px;
  text-transform: uppercase;
  color: #ffffff;
  font-weight: 600;
  margin-right: 12px;
`;

export const ItemContent = styled('div')``;
