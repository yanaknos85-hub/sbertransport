import { Divider as AndtDivider, Modal as AndtModal } from 'antd';
import styled from 'styled-components';

import { autocomplete } from '../styles';

export const StyledCargoType = styled('div')<any>`
  ${autocomplete}
`;

export const SelectDropdownButton = styled('button')<any>`
  display: flex;
  justify-content: flex-start;
  width: 100%;
  height: 100%;
  color: var(--jade) !important;
  background-color: var(--pure-white);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  border: none;
  margin: 5px 12px;
`;

export const Divider = styled(AndtDivider)<any>`
  margin: 4px 0;
`;

export const Modal = styled(AndtModal)<any>`
  .ant-modal-title {
    font-size: calc(var(--font-size-base) * 1.25) !important;
    font-family: 'SB Sans Interface SemiBold', serif, sans-serif;
    color: var(--gray-10);
  }

  .anticon {
    display: none;
  }

  .ant-modal-close-x {
    position: absolute;
    top: 0;
    right: 0;
    background: white no-repeat !important;
  }

  .ant-modal-header {
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
    border-bottom: none;
  }

  .ant-modal-footer {
    height: 70px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    border-top: none;
  }

  .ant-modal-body {
    p {
      color: var(--gray-7);
      font-family: 'SB Sans Text', serif, sans-serif;
      font-size: var(--fz-16) !important;
    }
  }

  .ant-modal-content {
    border: 1px solid var(--solitude);
    border-radius: 12px;
  }

  .ant-checkbox-wrapper {
    font-size: var(--fz-16) !important;
    color: var(--gray-10);
    font-family: 'SB Sans Text', serif, sans-serif;
  }

  .ant-input {
    border: 1px solid var(--input-border-color);
    border-radius: 8px;
    padding: 12px var(--padding-half);
    box-shadow: none;
  }

  .ant-input:hover {
    margin: -1px;
    border: 2px solid var(--input-border-color) !important;
    box-shadow: none;
    transition: all 0.3s linear;
  }

  .ant-input:focus {
    margin: -1px;
    border: 2px solid var(--jade) !important;
    box-shadow: none !important;
    transition: all 0.3s linear;
    outline: none;
  }

  .ant-input:active {
    margin: -1px;
    box-shadow: none;
  }

  .ant-btn {
    border-radius: 8px;
    border: none;
  }

  .ant-btn:hover {
    border: none;
  }

  .ant-btn:active {
    border: none;
  }

  .ant-input-number {
    border-radius: 8px;
    padding: 9px 16px;
    width: 100%;
  }

  .ant-input-number-input {
    height: 30px !important;
    padding-right: 0 !important;
  }
`;
