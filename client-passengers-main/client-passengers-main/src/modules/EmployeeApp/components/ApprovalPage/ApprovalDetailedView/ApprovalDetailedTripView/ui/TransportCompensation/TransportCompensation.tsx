import React, { type FC } from 'react';

import { useImageUrl } from 'shared/hooks/useImageUrl';
import { SavedFileInfo, UsedMIMEFileFormatEnum } from 'stores/Trip/Trip.interface';
import { CompensationModal } from './CompensationModal';
import { DOWNLOAD_FILE } from 'constants/constants.env';

interface TransportCompensationProps {
  document: SavedFileInfo;
}

export const TransportCompensation: FC<TransportCompensationProps> = ({ document }): JSX.Element => {
  const { imageUrl, isLoading } = useImageUrl(
    `${DOWNLOAD_FILE}/${document?.id}`,
    UsedMIMEFileFormatEnum[document?.fileFormat || 'JPEG']
  );

  return (
    <CompensationModal
      inProgress={isLoading}
      imageUrl={imageUrl}
      documentData={document}
    />
  );
};
