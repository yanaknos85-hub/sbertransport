/* eslint-disable @typescript-eslint/no-explicit-any */

import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { ILogger } from '@sber-sbertransport/mf-core';
import { UploadProps } from 'antd';
import React, { Dispatch, SetStateAction } from 'react';

import { UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';

import { fileTypeErrorMessage } from '../../constants';

import styles from '../../style.module.scss';

const maxFileSizeMB = 2;

export enum FileStatus {
  UPLOADING = 'uploading',
  DONE = 'done',
}

const getIsHeicFormat = (file: any): boolean => file.type === UsedMIMEFileFormatEnum.HEIC
  || (file.type === '' && file.name.includes(`.${UsedMIMEFileFormatEnum.HEIC}`) && /\.heic$/.test(file.name));

const getIsPermissibleFormat = (file: any): boolean => file.type === UsedMIMEFileFormatEnum.JPG
  || file.type === UsedMIMEFileFormatEnum.JPEG
  || file.type === UsedMIMEFileFormatEnum.PNG
  || file.type === UsedMIMEFileFormatEnum.TIFF
  || file.type === UsedMIMEFileFormatEnum.PDF
  || file.type === UsedMIMEFileFormatEnum.DOC
  || file.type === UsedMIMEFileFormatEnum.DOCX
  || getIsHeicFormat(file);

export const beforeUpload = (
  file: File,
  logger: ILogger,
  setImageUrl: Dispatch<SetStateAction<string | undefined>>
): boolean => {
  const isCorrectType = getIsPermissibleFormat(file);
  const isCorrectSize = file.size / 1024 / 1024 <= maxFileSizeMB;
  if (!isCorrectSize || !isCorrectType) {
    logger.toMessage('error', fileTypeErrorMessage);
    setImageUrl(undefined);
  }
  return isCorrectType && isCorrectSize;
};

export const getBase64 = (img: File, callback: (result: string | ArrayBuffer | null) => void): void => {
  const reader = new FileReader();
  reader.addEventListener('load', () => callback(reader.result));
  reader.readAsDataURL(img);
};

export const getFileAvatar = (file: File, imageUrl: string): JSX.Element => file.type === UsedMIMEFileFormatEnum.JPG
  || file.type === UsedMIMEFileFormatEnum.JPEG
  || file.type === UsedMIMEFileFormatEnum.PNG ? (
    <img
      src={imageUrl}
      alt={file?.name}
      className={styles.imageSize}
    />
  ) : (
    <div className={styles.fullScreen}>
      <span>{file.name}</span>
    </div>
  );

export const customRequest: UploadProps['customRequest'] = e => {
  // FIXME any
  setTimeout((e.onSuccess as any)('ok'), 0);
};

export const uploadButton = (loading: boolean): JSX.Element => (
  <div>
    {loading ? <LoadingOutlined /> : <PlusOutlined />}
    <div className="ant-upload-text">Upload</div>
  </div>
);
