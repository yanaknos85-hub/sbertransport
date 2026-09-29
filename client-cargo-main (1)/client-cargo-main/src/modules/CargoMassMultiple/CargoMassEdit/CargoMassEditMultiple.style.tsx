/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const Container = styled('div')``;

export const Content = styled('div')``;

export const Header = styled('div')`
  width: 100%;
  font-family: "SB Sans Interface", serif, sans-serif;
  font-size: 20px;
  line-height: 28px;
  font-weight: 600;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
`;

export const Block = styled('div')`
  margin: 16px 0;
  background: #ffffff;
  box-shadow: 0 1px rgba(0, 0, 0, 0.12), 0 3px 10px -4px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
`;

export const BlockPadding = styled('div')`
  padding: 20px 24px;
`;

export const BlockName = styled('div')`
  font-family: SB Sans Interface, serif, sans-serif;
  font-size: 18px;
  line-height: 24px;
  color: #262626;
  font-weight: 600;
  margin-bottom: 17px;

  span {
    color: #adadad;
  }
`;

export const BlockContent = styled('div')``;

export const Buttons = styled('div')`
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
`;
