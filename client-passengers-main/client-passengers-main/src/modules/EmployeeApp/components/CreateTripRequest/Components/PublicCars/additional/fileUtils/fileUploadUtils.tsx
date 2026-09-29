/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';
import { UploadProps } from 'antd';
import React, { Dispatch, SetStateAction } from 'react';

import { UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';

import { fileTypeErrorMessage, tripsInfoTitle } from '../../constants';

import styles from '../../style.module.scss';

import FileJPEG from 'shared/components/Images/file/FileJPEG.svg';
import FileJPG from 'shared/components/Images/file/FileJPG.svg';
import FilePDF from 'shared/components/Images/file/FilePDF.svg';
import FilePNG from 'shared/components/Images/file/FilePNG.svg';
import Close from 'shared/components/Images/closeGray.svg';
import { FormInstance } from 'antd/es/form/Form';
import { PublicInfoCard } from 'stores/TransportTypes/TransportTypes.interface';
import { FileCompensation } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';

const maxFileSizeMB = 2;
export interface ICustomRequest {
  onSuccess?: (body: any, xhr?: XMLHttpRequest) => void;
}

const getIsHeicFormat = (file: File): boolean => file.type === UsedMIMEFileFormatEnum.HEIC
  || (file.type === '' && file.name.includes(`.${UsedMIMEFileFormatEnum.HEIC}`) && /\.heic$/.test(file.name));

const getIsPermissibleFormat = (file: File): boolean => file.type === UsedMIMEFileFormatEnum.JPG
  || file.type === UsedMIMEFileFormatEnum.JPEG
  || file.type === UsedMIMEFileFormatEnum.PNG
  || file.type === UsedMIMEFileFormatEnum.TIFF
  || file.type === UsedMIMEFileFormatEnum.PDF
  || file.type === UsedMIMEFileFormatEnum.DOC
  || file.type === UsedMIMEFileFormatEnum.DOCX
  || getIsHeicFormat(file);

export const beforeUpload = (
  file: File,
  logger: ILogger
): boolean => {
  const isCorrectType = getIsPermissibleFormat(file);
  const isCorrectSize = file.size / 1024 / 1024 <= maxFileSizeMB;
  if (!isCorrectSize || !isCorrectType) {
    logger.toMessage('error', fileTypeErrorMessage);
  }
  return isCorrectType && isCorrectSize;
};

export const getBase64 = (img: File, callback: (result: string | ArrayBuffer | null) => void): void => {
  const reader = new FileReader();
  reader.addEventListener('load', () => callback(reader.result));
  reader.readAsDataURL(img);
};

const imageFileType = (file: File): string => {
  if (file.type === UsedMIMEFileFormatEnum.JPG) {
    return FileJPG;
  }

  if (file.type === UsedMIMEFileFormatEnum.JPEG) {
    return FileJPEG;
  }

  if (file.type === UsedMIMEFileFormatEnum.PNG) {
    return FilePNG;
  }

  if (file.type === UsedMIMEFileFormatEnum.PDF) {
    return FilePDF;
  }

  return FilePDF;
};

const deleteFile = (
  setFile: Dispatch<SetStateAction<FileCompensation | undefined>>,
  form: FormInstance,
  compensationType: string,
  index: number
) => {
  setFile(undefined);
  form.setFieldsValue({
    tripsInfo: form
      .getFieldValue(tripsInfoTitle)
      .map((el: PublicInfoCard, i: number) => index === i && el.compensationType === compensationType
        ? {
          ...el, savedFileData: undefined, ticket: undefined,
        }
        : el
      ),
  });
};

const formatBytes = (bytes: number) => {
  const units = ['Байт', 'Кб', 'Мб', 'Гб', 'Тб'];
  let i = 0;
  let byte = bytes;

  for (i; byte > 1024; i++) {
    byte /= 1024;
  }

  return `${byte.toFixed(1)} ${units[i]}`;
};

const formatFileName = (fileName: string) => {
  const splitFileName = fileName.split('.');
  splitFileName[0] = splitFileName[0].slice(0, 10);
  return splitFileName.join('.');
};

export const getFileAvatar = (
  file: File,
  setFile: Dispatch<SetStateAction<FileCompensation | undefined>>,
  form: FormInstance,
  compensationType: string,
  index: number
): JSX.Element => (
  <div className={styles.fullScreen}>
    <img
      src={imageFileType(file)}
      alt={file?.name}
      className={styles.imageSize}
    />
    <div className={styles.fullScreen_info}>
      <div>
        {formatFileName(file.name)}
      </div>
      <span>{formatBytes(file.size)}</span>
    </div>
    <div className={styles.deleteImage} onClick={() => deleteFile(setFile, form, compensationType, index)}>
      <img src={Close} alt="deleteImage" />
    </div>
  </div>
);

export const customRequest: UploadProps['customRequest'] = e => {
  setTimeout((e.onSuccess as any)('ok'), 0);
};

export const uploadButton = (): JSX.Element => (
  <div>
    <div className="ant-upload-text">Выберите файл или перетяните его сюда</div>
  </div>
);
