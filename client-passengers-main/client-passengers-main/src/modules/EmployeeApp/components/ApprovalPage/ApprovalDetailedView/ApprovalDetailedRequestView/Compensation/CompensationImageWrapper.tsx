import React from 'react';

import { useImageUrl } from 'shared/hooks/useImageUrl';
import { SavedFileInfo, UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';

import CompensationImage from './CompensationImage';

interface CompensationImageWrapperProps {
  document: SavedFileInfo;
}

const CompensationImageWrapper = ({ document }: CompensationImageWrapperProps): JSX.Element => {
  const { imageUrl, isLoading } = useImageUrl(
    `/requests/files/download/${document?.id}`,
    UsedMIMEFileFormatEnum[document?.fileFormat || 'JPEG']
  );

  return (
    <CompensationImage
      inProgress={isLoading}
      imageUrl={imageUrl}
      document={document}
    />
  );
};

export default CompensationImageWrapper;
