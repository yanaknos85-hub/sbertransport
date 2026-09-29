import React from 'react';
import { Upload as AndtButton } from 'antd';
import styled from 'styled-components';

export const Upload = styled(props => <AndtButton {...props} />)`
  .ant-upload {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 120px;
    color: var(--gray-10,);
    background: #ffffff;
    border-radius: 12px;
    border: 1px dashed rgba(0, 0, 0, 0.3);
    font-family: 'SB Sans Text', sans-serif, serif;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    text-align: center;
  }

  .ant-upload.ant-upload-select {
    border: none;
  }

  .text {
    color: rgb(16, 191, 106);
  }
`;
