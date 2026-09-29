/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { Upload } from 'antd';
import styled from 'styled-components';

export const StyledDragger = styled(Upload)<any>`

  .ant-upload.ant-upload-select {
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    width: 100%;
    height: 64px;
    background: #ffffff;
    border: 0.5px dashed #d9d9d9;
    border-radius: 12px;
    cursor: pointer;
    -webkit-transition: border-color .3s;
    transition: border-color .3s;
  }

  .ant-upload-list.ant-upload-list-picture {
    .ant-upload-list-item-thumbnail {
      width: 42px;
      height: 42px;
      img {
        width: 42px;
        height: 42px;
      }
    }

    .ant-upload-list-item-thumbnail.ant-upload-list-item-file {
      display: none;
    }

    .ant-upload-list-item-name {
      color: rgb(38, 38, 38);
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      letter-spacing: -0.3px;
    }

    .ant-upload-list-item-list-type-picture {
      position: relative;
      height: 72px;
      padding: 15px 16px;
      border: 1px solid rgb(242, 243, 246);
      border-radius: 12px;
      background: rgb(242, 243, 246);
      display: table;
      min-width: 324px;
    }
  }
`;
