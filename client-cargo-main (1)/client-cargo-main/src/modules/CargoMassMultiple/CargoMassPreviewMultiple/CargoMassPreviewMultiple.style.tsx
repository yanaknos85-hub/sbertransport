import React from 'react';
import Modal, { ModalProps } from 'antd/lib/modal/Modal';
import Button from 'shared/ui/Button/Button';
import styled from 'styled-components';

export const ViewportStyled = styled.div`
  overflow: auto;
  border-radius: 8px 0 0 8px;
  max-height: 600px;
  border-top: 1px solid rgba(38, 38, 38, 0.08);
  border-bottom: 1px solid rgba(38, 38, 38, 0.08);
  border-left: 1px solid rgba(38, 38, 38, 0.08);
`;

export const TableStyled = styled.table``;

export const ThStyled = styled.th`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-weight: 200;
  color: #737373;
  font-size: 12px;
  line-height: 16px;
  padding: 20px 16px;
  text-align: left;
  box-sizing: border-box;
  white-space: nowrap;
  border-left: 1px solid rgba(38, 38, 38, 0.08);
  border-bottom: 1px solid rgba(38, 38, 38, 0.08);
  border-right: 1px solid rgba(38, 38, 38, 0.08);

  &:first-child {
    border-left: 0;
  }
  &:last-child {
    border-right: 0;
  }
`;

export const TdStyled = styled.td`
  font-size: 14px;
  line-height: 22px;
  padding: 25px 16px;
  text-align: left;
  box-sizing: border-box;
  border-top: 1px solid rgba(38, 38, 38, 0.08);
  border-left: 1px solid rgba(38, 38, 38, 0.08);
  border-right: 1px solid rgba(38, 38, 38, 0.08);

  &:first-child {
    border-left: 0;
  }
  &:last-child {
    border-right: 0;
  }
`;

export const TrStyled = styled.tr`
  &:nth-child(2n) {
    background: rgba(242, 243, 246, 0.4);
  }
`;

export const SpinContainer = styled('div')`
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 999;
`;

export const SpinWrapper = styled('div')`
  position: absolute;
  right: 5px;
  top: 30px;
`;

export const ModalStyled = styled(Modal)<ModalProps>`
  padding-top: 80px;
  -webkit-font-smoothing: antialiased;

  .ant-modal-content {
    border-radius: 12px;
    overflow: hidden;
  }

  .ant-modal-header {
    border: 0;
    padding: 20px;
  }

  .ant-modal-body {
    padding-top: 0;
    padding-right: 0;
    padding-bottom: 0;
  }

  .ant-modal-footer {
    border: 0;
    padding: 20px;
  }
`;

export const ModalTitleStyled = styled.div`
  font-family: 'SB Sans Interface', serif, sans-serif;;
  font-size: 20px;
  line-height: 28px;
`;

export const ModalSubTitleStyled = styled.div`
  font-size: 14px;
  line-height: 22px;
  color: #909090;
  margin-top: 3px;
`;

export const ButtonsStyled = styled.div`
  display: flex;
  justify-content: flex-end;
`;
export const ButtonCancelStyled = styled(props => (<Button $makeLikeLink={true} {...props} />))`
  max-width: 100px;
  font-size: 14px;
  line-height: 22px;
  height: 52px;
`;

export const ButtonApproveStyled = styled(Button)`
  max-width: 290px;
  font-size: 14px;
  line-height: 22px;
`;
