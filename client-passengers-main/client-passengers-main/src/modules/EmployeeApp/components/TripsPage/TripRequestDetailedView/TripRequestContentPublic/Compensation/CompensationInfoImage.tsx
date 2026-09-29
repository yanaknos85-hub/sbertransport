import React from 'react';

import { useImageUrl } from 'shared/hooks/useImageUrl';
import { SavedFileInfo, UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';
import CompensationDataImage from './CompensationDataImage';

interface CompensationInfoImageProps {
  document: SavedFileInfo;
}

const CompensationInfoImage = ({ document }: CompensationInfoImageProps): JSX.Element => {
  const { imageUrl, isLoading } = useImageUrl(
    `/requests/files/download/${document?.id}`,
    UsedMIMEFileFormatEnum[document?.fileFormat || 'PDF']
  );

  return (
    <CompensationDataImage
      inProgress={isLoading}
      imageUrl={imageUrl}
      documentData={document}
    />
  );
};

export default CompensationInfoImage;
