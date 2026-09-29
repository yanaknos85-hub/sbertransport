import React, { Suspense } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import DownloadButton from 'shared/components/DownloadButton/DownloadButton';
import { importExportEndpointMap } from '../../UploadButton';

const GeoZonesHandbookComponent: React.FC<{ theme?: 'primary' | 'secondary' }> = ({ theme = 'primary' }) => (
  <Suspense fallback={<SpinWrapped />}>
    <DownloadButton url={`${importExportEndpointMap.zones}/files/zones`} theme={theme} />
  </Suspense>
);

export default GeoZonesHandbookComponent;
