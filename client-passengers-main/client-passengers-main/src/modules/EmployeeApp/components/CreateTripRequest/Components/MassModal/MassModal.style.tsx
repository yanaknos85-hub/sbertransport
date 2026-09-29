import Modal, { ModalProps } from 'antd/lib/modal/Modal';
import styled from 'styled-components';

export const ModalStyled = styled(Modal)<ModalProps>`
  padding-top: 80px;
  -webkit-font-smoothing: antialiased;

  .ant-modal-body {
    padding: 0 24px 32px 24px;
  }

  .ant-modal-content {
    border-radius: 12px;
    overflow: hidden;
  }

  .ant-modal-header {
    border: 0;
    padding: 20px;
  }

  .ant-modal-title {
    font-size: 24px;
    font-weight: 600;
  }

  .ant-modal-content {
    border-radius: 20px !important;

    & > button {
      display: inline !important;
    }
  }

  .ant-modal-close {
    top: 5px !important;

  }

  .ant-modal-footer {
    border: 0;
    padding: 20px;
  }

  .downLoad {
    width: 51%;
    margin-bottom: 24px;
    color: rgb(16, 191, 106);
    cursor: pointer;
  }

  .descriptionWrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .description {
    flex: 4 0 0;
  }

  .img {
    display: flex;
    justify-content: flex-end;
    flex: 1 0 0;
  }
`;
