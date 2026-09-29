/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled, { css } from 'styled-components';

export const WaypointsStyled = styled('div')<any>`
  position: relative;
`;

export const WaypointStyled = styled('div')<any>`
  display: ${props => (props.hidden ? 'none' : 'flex')};
  position: relative;
  margin-top: 16px;

  &:first-child {
    margin-top: 0;
  }

  &:last-child {
    &::after {
      height: 0;
    }
  }

  &::after {
    content: '';
    position: absolute;
    width: 3px;
    left: 7px;
    top: 18px;
    height: ${props => (props.open ? '100%' : 'calc(100% + 15px)')};

    ${props => {
    if (!props.open) {
      return css`
          background: linear-gradient(#10bf6a, #6979f7);
        `;
    }
    if (props.first) {
      return css`
          background: linear-gradient(#10bf6a, #909090);
        `;
    }
    if (props.lastButOne) {
      return css`
          background: linear-gradient(#909090, #6979f7);
        `;
    }
    return css`
        background: #a0a8b0;
      `;
  }}
  }
`;

export const BulletStyled = styled('div')<any>`
  top: ${props => (!props.hideLabels && (props.first || (!props.open && props.last)) ? '10px' : '0')};
  position: relative;
  width: 18px;
  min-width: 18px;
  height: 18px;
  border-radius: 50%;
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-weight: bold;
  font-size: 10px;
  line-height: 12px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;

  ${props => {
    if (props.first) {
      return css`
        background: #10bf6a;
      `;
    }
    if (props.last) {
      return css`
        background: #6979f7;
      `;
    }
    return css`
      background: #a0a8b0;
    `;
  }}
`;

export const AddressStyled = styled.div`
  margin-left: 12px;
`;

export const LabelStyled = styled.div`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 12px;
  line-height: 14px;
  color: #909090;
`;

export const ValueStyled = styled('div')<any>`
  ${props => props.link
  && css`
      cursor: pointer;
      border-bottom: 1px dotted;
    `};

  font-family: 'SB Sans Interface', serif, sans-serif;
  color: #000000;
  font-size: 16px;
  line-height: 20px;
  box-sizing: border-box;
`;
