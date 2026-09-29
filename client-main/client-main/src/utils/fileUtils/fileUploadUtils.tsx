/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';

import { UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';

export const fileTypeErrorMessage
  = 'Необходимо добавить файл формата .jpg, .jpeg, .png, .tiff, .heic ';

const getIsHeicFormat = (file: File): boolean => file.type === UsedMIMEFileFormatEnum.HEIC
  || (file.type === '' && file.name.includes(`.${UsedMIMEFileFormatEnum.HEIC}`) && /\.heic$/.test(file.name));

const getIsPermissibleFormat = (file: File): boolean => file.type === UsedMIMEFileFormatEnum.JPG
  || file.type === UsedMIMEFileFormatEnum.JPEG
  || file.type === UsedMIMEFileFormatEnum.PNG
  || file.type === UsedMIMEFileFormatEnum.TIFF
  || getIsHeicFormat(file);

export const beforeUpload = (
  file: File,
  logger: ILogger
): boolean => {
  const isCorrectType = getIsPermissibleFormat(file);

  if (!isCorrectType) {
    logger.toMessage('error', fileTypeErrorMessage);
  }

  return isCorrectType;
};
