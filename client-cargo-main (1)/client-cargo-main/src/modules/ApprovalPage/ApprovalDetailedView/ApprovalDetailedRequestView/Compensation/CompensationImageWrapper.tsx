import React from 'react';
import { useImageUrl } from 'shared/hooks/useImageUrl';

import { SavedFileInfo, UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';

import CompensationImage from './CompensationImage';

interface CompensationImageWrapperProps {
  document: SavedFileInfo;
  requestIsLoading: boolean;
}

const CompensationImageWrapper = ({ document, requestIsLoading }: CompensationImageWrapperProps): JSX.Element => {
  const { imageUrl, isLoading: imageIsLoading } = useImageUrl(
    `/requests/files/download/${document?.folder}/${document?.fileName}`,
    UsedMIMEFileFormatEnum[document?.fileFormat || 'JPEG']
  );

  return (
    <CompensationImage
      inProgress={imageIsLoading || requestIsLoading}
      imageUrl={imageUrl}
      document={document}
    />
  );
};

export default CompensationImageWrapper;
