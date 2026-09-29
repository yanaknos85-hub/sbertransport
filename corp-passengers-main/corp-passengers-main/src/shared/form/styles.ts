/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { css } from 'styled-components';

export const inputHeight = '48px';
export const inputPadding = '13px 16px';
export const border = '1px solid #e0e0e0';
export const borderHover = '1px solid #31cc7c';
export const borderFocus = '1px solid #31cc7c';
export const borderRaduis = '8px';
export const shadowFocused = '0 0 0 1px rgb(16 191 106 / 100%)';

export const baseInput = css`
  border: ${border};
  box-sizing: border-box;
  border-radius: ${borderRaduis}!important;
  height: ${inputHeight};
  font-family: SB Sans Text;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: #262626;
  width: 100%;
`;

export const inputFocus = css`
  border: ${borderFocus};
  box-shadow: ${shadowFocused};
  outline: none;
`;

export const inputHover = css`
  border: ${borderHover};
  outline: none;
`;

export const input = css`
  ${baseInput};
  padding: ${inputPadding};

  &:hover {
    ${inputHover}
  }

  &:focus {
    ${inputFocus}
  }
`;

export const autocomplete = css`
  .ant-select {
    width: 100%;
  }

  .ant-select-selection-search-input {
    ${baseInput};
    padding: ${inputPadding};
  }

  .ant-select-selection-search-input:focus {
    ${inputFocus}
  }

  .ant-select-selection-search-input:hover {
    ${inputHover}
  }

  .ant-select-selection-placeholder {
    top: 8px;
  }
`;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const rate = css<any>`
  font-size: 36px;
  color: ${props => (props.isGreenStar || props.rate > props.middleRate ? 'var(--jade)' : 'var(--neon-carrot)')};
  p {
    margin-bottom: 4px;
  }
`;
