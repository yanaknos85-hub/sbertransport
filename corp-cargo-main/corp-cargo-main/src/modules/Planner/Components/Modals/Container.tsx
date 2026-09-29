import React, { FC } from 'react';

import { Modal as ModalAnt } from 'antd';

import styled from 'styled-components';
import { ReactComponent as CrossIcon } from '../../images/crossIcon.svg';

const Modal = styled(ModalAnt)`
  .ant-modal-content {
    border-radius: 12px;
  }

  .ant-modal-content .ant-modal-header {
    border-radius: 12px 12px 0 0;
  }

  .ant-modal-close-x svg {
  }
`;

export const Container: FC<any> = ({ children, ...props }) => (
  <Modal closeIcon={<CrossIcon />} {...props}>
    {children}
  </Modal>
);
