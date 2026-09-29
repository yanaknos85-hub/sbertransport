import styled, { css, FlattenInterpolation } from 'styled-components';
import { TabProps } from './Types';

const DP = 8;
const DP_HALF = 4;

export enum SizeKind {
  None,
  Xs,
  S,
  M,
  L,
  Xl,
  Xxl,
}

export function cubicBezier(x1: number, y1: number, x2: number, y2: number): string {
  return `cubic-bezier(${x1}, ${y1}, ${x2}, ${y2})`;
}

export const ENTER_TIME = 120;
export const LINEAR = cubicBezier(0, 0, 1, 1);

const StyledTab = styled.span<TabProps>`
  transition: background-color ${ENTER_TIME}ms ${LINEAR}, color ${ENTER_TIME}ms ${LINEAR},
  stroke ${ENTER_TIME}ms ${LINEAR};
  box-sizing: border-box;
  display: flex;
  flex-direction: ${props => props.column ? 'column' : 'row'};
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  background: transparent;
  color: black;
  user-select: none;
  height: inherit;
  line-height: inherit;
  border: 1px solid #0000000F;

  &:focus {
    border: ${props => props.isReadOnly ? '1px solid #0000000F' : '1px solid #A8ABB3'};
    outline: ${props => props.isReadOnly ? 'none' : '1px solid #A8ABB3'};
  }

  &:hover {
    border: 1px solid #0000000F;
    outline: ${props => props.isReadOnly ? 'none' : '1px solid #0000001F'};
  }

  &:not(:first-child) {
    margin-left: 24px;
  }

  .icon {
    transition: stroke ${ENTER_TIME}ms ${LINEAR};
    pointer-events: none;
    stroke: black;
  }

  ${props => {
    let styles: FlattenInterpolation<any> | undefined;

    if (props.isActive) {
      styles = css`
        cursor: pointer;
        color: black;
        border: 1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'};
        outline: 1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'};

        &:hover {
          border: ${!props.isReadOnly ? `1px solid ${props.isPositive ? '#0EA85D' : '#E0882C'}` : `1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'}`};
          outline: ${!props.isReadOnly ? `1px solid ${props.isPositive ? '#0EA85D' : '#E0882C'}` : `1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'}`};
        }

         &:focus {
          border: ${!props.isReadOnly ? `1px solid ${props.isPositive ? '#0C9151' : '#C27526'}` : `1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'}`};
          outline: ${!props.isReadOnly ? `1px solid ${props.isPositive ? '#0C9151' : '#C27526'}` : `1px solid ${props.isPositive ? '#10BF6A' : '#FF9A32'}`};
        }
      `;
    }

    return styles;
  }}

  ${props => {
    let styles: FlattenInterpolation<any> | undefined;

    if (props.size === SizeKind.Xs) {
      styles = tabSize(DP_HALF, DP_HALF);
    } else if (props.size === SizeKind.S) {
      styles = tabSize(DP, DP);
    } else if (props.size === SizeKind.M) {
      styles = tabSize(14, 10);
    } else if (props.size === SizeKind.L) {
      styles = tabSize(DP + 0.5, DP);
    } else if (props.size === SizeKind.Xl) {
      styles = tabSize(DP, DP);
    }

    return styles;
  }}
`;

function tabSize(indent: number, round: number): FlattenInterpolation<any> {
  return css`
    padding: 0 ${indent}px;
    border-radius: ${round}px;
  `;
}

export default StyledTab;
