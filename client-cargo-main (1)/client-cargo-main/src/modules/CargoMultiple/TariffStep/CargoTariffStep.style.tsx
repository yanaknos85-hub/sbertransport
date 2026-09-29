/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Popover as AndtPopover } from 'antd';
import { ReactComponent as Icon } from 'shared/images/cargo/question.svg';
import styled, { css } from 'styled-components';

export const SideSections = styled('div')`
  overflow-x: hidden;
  overflow-y: auto;
  margin-top: 12px;
  background-color: rgba(0, 0, 0, 0);
  border-radius: 16px;
`;

export const Tariffs = styled('div')`
  padding: 16px;
  background: #fff;
  border-radius: 16px;
`;

export const TariffsTitle = styled('div')`
  font-family: 'SB Sans Text', serif, sans-serif;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: #262626;
  margin-bottom: 0;
`;

// --- Tariff component

export const TariffCard = styled('div')<any>`
  margin-top: 9px;
  cursor: pointer;
  display: flex;
  align-items: center;
  width: 100%;
  padding: 16px 16px 16px 0;
  border-radius: 10px;
  border: 1px solid ${props => (props.active ? '#10bf6a' : 'rgba(38, 38, 38, 0.08)')};
  background: ${props => (props.active ? 'rgba(16, 191, 106, 0.02)' : '#FFF')};
  box-sizing: border-box;

  &:first-child {
    margin-top: 0;
  }

  &:hover {
    border: 1px solid ${props => (props.disabled ? 'rgba(38, 38, 38, 0.08)' : '#10bf6a')};
  }

  ${props => props.disabled
  && css`
      cursor: not-allowed;
      opacity: 0.7;
    `}
`;

export const TariffIcon = styled('div')`
  display: flex;
  justify-content: center;
  flex: 1 0 0;
  width: 48px;
  height: 48px;
`;

export const TariffIconImg = styled('img')``;

export const TariffDescription = styled('div')`
  flex: 4 0 0;
`;

export const TariffTitle = styled('div')`
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'SB Sans Text SemiBold', serif, sans-serif;
  font-size: 14px;
  font-weight: 600;
`;

export const TariffDisabled = styled('div')`
  font-weight: 300;
  font-size: 12px;
  color: #ff5743;
  opacity: 1;
  font-family: 'SB Sans Text', serif, sans-serif;
`;

export const TariffText = styled('div')`
  display: flex;
  justify-content: space-between;
  font-size: 12px;
`;

export const TariffTime = styled('div')`
  color: #10bf6a;
  display: flex;
`;
export const TariffDay = styled('span')`
  align-items: flex-end;
`;

export const DisabledIcon = styled(Icon)`
  width: 24px;
  height: 24px;
  margin-left: 6px;
`;

export const Popover = styled(AndtPopover)``;

export const PopoverContent = styled('div')`
  background-color: #262c31;
  color: rgba(255, 255, 255, 0.8);
`;

export const PopoverLink = styled('a')`
  color: #10bf6a;

  &:hover {
    color: #31cc7c;
  }

  &:active {
    color: #059956;
  }
`;

export const EmptyTariffs = styled('div')`
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px;
  width: 100%;
  border: 1px solid rgba(38, 38, 38, 0.08);
  border-radius: 12px;
  background: var(--pure-white);
`;

export const FormContainer = styled('div')`
  padding: 16px;
  margin-top: 12px;
  background: var(--pure-white);
  border-radius: 16px;
`;
