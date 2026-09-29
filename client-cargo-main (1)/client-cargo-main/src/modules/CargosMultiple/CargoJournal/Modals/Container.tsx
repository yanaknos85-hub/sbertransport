import React, { FC } from 'react';
import { Modal as ModalAnt } from 'antd';
import { ModalProps } from 'antd/lib/modal/Modal';
import styled from 'styled-components';

import { ReactComponent as CrossIcon } from '../../static/images/closeCross.svg';

const Modal = styled(ModalAnt)`
  .ant-modal-content {
    border-radius: 12px;
  }

  .ant-modal-content .ant-modal-header {
    border-radius: 12px 12px 0 0;
  }
`;

export const Container: FC<ModalProps> = ({ children, ...props }) => (
  <Modal closeIcon={<CrossIcon />} {...props}>
    {children}
  </Modal>
);
