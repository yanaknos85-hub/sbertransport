/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled, { css } from 'styled-components';

export const Days = styled('div')<{ first: boolean }>`
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
  font-family: "SB Sans Text", serif, sans-serif;
  font-size: 16px;
  line-height: 24px;
  letter-spacing: -0.3px;
  padding: 4px 12px;
  display: inline-block;
  margin: ${props => (props.first ? 0 : 20)}px 0 20px 0;

  span {
    font-size: 14px;
    line-height: 22px;
    color: #4d4d4d;
    font-weight: 600;
    margin-top: -1px;
  }
`;

export const Item = styled('div')`
  flex: 1 1 424px;
  color: var(--ship-cove);
  box-sizing: border-box;
  border-radius: 12px;
  background-color: white;
  box-shadow: 0 4px 12px 4px rgba(4, 0, 58, 0.04), 0 0 1px 0 rgba(3, 0, 27, 0.04);
`;

export const Inner = styled('div')<{
  flexCenter?: boolean;
  borderRight?: boolean;
  paddingTop?: string;
  paddingLeft?: string;
  paddingBottom?: string;
  paddingRight?: string;
  flexEnd?: boolean;
}>`
  position: relative;
  border-right: ${props => (props.borderRight ? '1px solid rgba(38, 38, 38, 0.08)' : 0)};
  padding-top: ${props => props.paddingTop ?? '24px'};
  padding-right: ${props => props.paddingRight ?? '24px'};
  padding-bottom: ${props => props.paddingBottom ?? '24px'};
  padding-left: ${props => props.paddingLeft ?? '24px'};
  ${props => props.flexCenter
  && css`
      justify-content: center;
      display: flex;
      flex-direction: column;
      height: 100%;
    `};

  ${props => props.flexEnd
  && css`
      justify-content: flex-end;
      display: flex;
      flex-direction: column;
      height: 100%;
    `};
`;

export const Header = styled('div')`
  position: relative;
`;

export const RequestID = styled('div')`
  display: flex;
  margin-bottom: 4px;
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 14px;
  line-height: 22px;
  font-weight: 600;
  color: #262626;
`;

export const Icons = styled('div')`
  display: flex;
  position: absolute;
  right: 0;
  top: 0;
  cursor: pointer;

  svg:first-child {
    margin-right: 12px;
  }
`;

export const Cargos = styled('div')`
  display: flex;
  margin-top: 4px;
  width: 90%;
`;

export const CargosList = styled('div')``;

const cargoStyles = css`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 14px;
  line-height: 16px;
  color: #909090;
`;

export const CargosItem = styled('div')`
  ${cargoStyles};
`;

export const Dot = styled('div')`
  ${cargoStyles};
  margin: 0 4px;
`;

export const CargosParams = styled('div')`
  display: flex;
  ${cargoStyles};
`;

export const CargoTypes = styled('div')`
  margin-top: 16px;
  display: flex;
`;

export const CargoType = styled('div')`
  margin-right: 8px;
`;

export const Divider = styled('div')`
  background: rgba(38, 38, 38, 0.08);
  width: 100%;
  height: 1px;
`;

export const Params = styled('div')<any>`
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  min-width: 200px;

  ${props => props.value
  && css`
      margin-top: 8px;
    `}
`;

export const Param = styled('div')<{ first?: boolean }>`
  margin-right: 15px;
  min-width: 64px;
  width: 326px;
  ${props => props.first
  && css`
      margin-right: 35px;
    `}
`;

export const Label = styled('div')<{ price?: boolean }>`
  font-family: "SB Sans Interface", serif, sans-serif;
  font-size: 12px;
  line-height: 16px;
  letter-spacing: 0.2px;
  margin-bottom: 8px;
`;

export const Value = styled('div')<{ price?: boolean }>`
  font-family: "SB Sans Text", serif, sans-serif;
  color: #262626;
  margin-top: 16px;
  font-size: ${props => (props.price ? 24 : 16)}px;
  font-weight: ${props => (props.price ? 600 : 'normal')};
  line-height: 16px;
`;
