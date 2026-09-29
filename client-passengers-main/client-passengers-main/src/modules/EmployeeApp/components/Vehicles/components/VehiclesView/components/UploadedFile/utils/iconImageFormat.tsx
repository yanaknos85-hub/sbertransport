import React, { ReactComponentElement } from 'react';
import { UsedFileFormatEnum } from 'stores/Trip/Trip.interface';
import { ReactComponent as FileJPEG } from '../images/FileJPEG.svg';
import { ReactComponent as FileJPG } from '../images/FileJPG.svg';
import { ReactComponent as FilePDF } from '../images/FilePDF.svg';
import { ReactComponent as FilePNG } from '../images/FilePNG.svg';
import { ReactComponent as FileTIFF } from '../images/FileTIFF.svg';
import { ReactComponent as FileHEIF } from '../images/FileHEIF.svg';

const fileIcons: Partial<Record<UsedFileFormatEnum, ReactComponentElement<any>>> = {
  [UsedFileFormatEnum.png]: <FilePNG />,
  [UsedFileFormatEnum.jpeg]: <FileJPEG />,
  [UsedFileFormatEnum.jpg]: <FileJPG />,
  [UsedFileFormatEnum.tiff]: <FileTIFF />,
  [UsedFileFormatEnum.heif]: <FileHEIF />,
  [UsedFileFormatEnum.pdf]: <FilePDF />,
};

export const iconImageFormat = (fileFormat: UsedFileFormatEnum) => {
  return fileIcons[fileFormat];
};
