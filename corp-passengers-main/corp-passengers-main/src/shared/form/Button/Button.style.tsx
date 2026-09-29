/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled, { css } from 'styled-components';
import { Button } from 'antd';

const baseButtonStyle = css`
  outline: 0;
  border: 0;
  padding: 0;
  box-shadow: none;
  display: inline-block;
  background: transparent;
  user-select: none;
  touch-action: manipulation;
  -webkit-appearance: none !important;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
`;

const fontButtonStyle = css`
  font-family: SB Sans Text;
  font-style: normal;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const StyledIconButton = styled('button')<any>`
  ${baseButtonStyle}
`;

// Это не антовская кнопка
export const StyledBaseButton = styled('button')<any>`
  ${baseButtonStyle}
  ${fontButtonStyle}
`;

export const StyledButton = styled(Button)<any>`
  ${fontButtonStyle};

  min-width: 124px;
  height: 48px;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  color: ${props => (props.color === 'transparent' ? '#262626' : '#FFFFFF')};
  background: ${props => (props.color === 'transparent' ? 'transparent' : '#10BF6A')};
  border-color: ${props => (props.color === 'transparent' ? 'transparent' : '#10BF6A')};

  &::after {
    display: none !important;
  }

  &:focus,
  &:hover {
    color: #10bf6a;
    background: ${props => (props.color === 'transparent' ? 'transparent' : '#FFFFFF')};
    border-color: ${props => (props.color === 'transparent' ? 'transparent' : '#10BF6A')};
  }
`;
