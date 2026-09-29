import Modal from 'antd/lib/modal/Modal';
import React, { FC } from 'react';
import styled from 'styled-components';

const StyledModel = styled(Modal)`
  color: var(--black-text);
  font-family: 'SB Sans Text';
  letter-spacing: -0.30000001192092896px;
  // padding: 40px;

  .ant-modal-content {
    border-radius: 8px;
    height: 490px;

    :global(.ant-modal) {
      // width: 554px;
      left: -60px;
    }
  }

  .ant-modal-header {
    border-radius: 8px 8px 0 0;
  }

  .ant-modal-title {
    font-size: 24px;
    line-height: 28px;
    font-family: 'SB Sans Text Bold';
  }

  .ant-modal-body {
    h3 {
      font-size: 12px;
      line-height: 20px;
      color: var(--black-shading-text-2);
    }

    h2 {
      font-size: 18px;
      line-height: 24px;
      color: black;
      font-family: 'SB Sans Text Bold';
    }

    p {
      font-size: 14px;
      line-height: 22px;
      color: var(--black-shading-text-2);

      b {
        color: var(--black-text);
        font-weight: 800;
      }
    }
  }

  .ant-modal-footer {
    text-align: center;
    padding-top: 22px;
  }
`;

/**
 * TForm - кастомный компонент формы проекта Сбертранспорт
 * */

const TModal: FC<any> = props => <StyledModel {...props} />;

export default TModal;
