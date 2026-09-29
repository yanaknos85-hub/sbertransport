import React, { Suspense } from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { FileExcelOutlined } from '@ant-design/icons';
import DownloadButton from 'shared/components/DownloadButton/DownloadButton';
import { importExportEndpointMap } from '../UploadButton';

export const WorkingGroupsHandbookComponent: React.FC<{ theme?: 'primary' | 'secondary' }> = ({
  theme = 'primary',
}): JSX.Element => (
  <Suspense fallback={<SpinWrapped />}>
    <DownloadButton
      theme={theme}
      url={`${importExportEndpointMap.workingGroups}/files/workgroup`}
      formats={[{ ext: 'XLSX', icon: <FileExcelOutlined /> }]}
    />
  </Suspense>
);

export default WorkingGroupsHandbookComponent;
