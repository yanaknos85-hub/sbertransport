/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import Button from 'shared/ui/Button/Button';
import styled, { css } from 'styled-components';

export const Container = styled('div')`
  display: flex;
`;

export const Content = styled('div')`
  flex: 2 0 0;
  margin-top: 48px;
`;

export const Fixed = styled('div')<{ flex: boolean }>`
  position: fixed;
  width: 100%;

  ${props => props.flex
  && css`
      display: flex;
      justify-content: space-between;
    `};
`;

export const Header = styled('div')`
  position: fixed;
  z-index: 2;
  width: 100%;
  font-family: "SB Sans Interface", serif, sans-serif;
  font-size: 20px;
  line-height: 28px;
  font-weight: 600;
  padding-bottom: 20px;
  background: #f7f8fa;
`;

export const HeaderInner = styled('div')`
  display: flex;
  justify-content: space-between;
`;

export const HeaderItem = styled('div')`
  display: flex;
`;

export const Count = styled('div')`
  color: #adadad;
  margin-left: 8px;
`;

export const List = styled('div')``;

export const Item = styled('div')`
  margin-top: 12px;

  &:first-child {
    margin-top: 0;
  }
`;

export const Aside = styled('div')`
  flex: 1 0 0;
  margin-top: 48px;
  margin-left: 16px;
`;

export const SelectTariff = styled('div')`
  margin-bottom: 24px;
`;

export const LabelSelectTariff = styled('div')`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 12px;
  line-height: 16px;
  color: #909090;
  margin-bottom: 8px;
`;

export const ButtonOrder = styled(Button)`
  margin-top: 24px;
  font-size: 14px;
  line-height: 22px;
  height: 48px;
`;
