/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { FC, MouseEvent } from 'react';
import { UploadResponse } from 'api/upload';
import { formatBytes } from 'utils/formatBytes';
import { ReactComponent as IconCross } from './images/cross.svg';
import { useImageUrl } from 'shared/hooks/useImageUrl';
import { UsedFileFormatEnum } from 'stores/Trip/Trip.interface';

import styles from './uploadedFile.module.scss';
import { useModalState } from 'shared/hooks/useModal';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { Modal } from 'antd';
import { SpinWrapped } from 'shared/components';
import { validFormatFile } from 'modules/EmployeeApp/components/Vehicles/constants/vehicles.constants';
import { iconImageFormat } from './utils/iconImageFormat';
import { ReactComponent as IconFile } from './images/file.svg';
import { UsedPersonalTransportFileFormatEnum } from '../../types/vehiclesForm.types';

interface Props {
  file: UploadResponse | null;
  actions: any;
  setFiles?: () => void;
}

const UploadedFile: FC<Props> = ({
  file, actions, setFiles,
}) => {
  const [modal, modalActions] = useModalState();
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();
  const { selfEmployee } = employeeStore;
  const {
    organizationId, departmentId, id: employeeId,
  } = selfEmployee;
  const { imageUrl, isLoading } = useImageUrl(
    `/organizations/${organizationId}/departments/${departmentId}/employees/${employeeId}/files/download/${file?.fileName}`,
    UsedPersonalTransportFileFormatEnum[file?.fileFormat || 'JPEG']
  );

  if (!file) {
    return null;
  }

  const validFile = file.fileFormat && validFormatFile.includes(file.fileFormat);
  const fileName = file.fileName?.split('_').slice(4).join(' ');
  const handleDownload = () => {
    if (file && file?.fileName) {
      const url = imageUrl;
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file?.fileName);
      document.body.appendChild(link);
      link.click();
    }
  };

  const handleShowFile = () => {
    if (file.fileFormat !== UsedFileFormatEnum.pdf) {
      modalActions.show();
    } else {
      handleDownload();
    }
  };

  const handleDeleteFile = (e: MouseEvent<any>) => {
    if (setFiles) {
      setFiles();
    }
    actions.remove();
    e.stopPropagation();
  };

  return (
    <div className={styles.file} onClick={handleShowFile}>
      {isLoading ? (
        <SpinWrapped className={styles.compensationImageSpinner} />
      )
        : (
          <>
            {file?.fileFormat && validFile ? iconImageFormat(file.fileFormat) : <IconFile />}
            <div className={styles.desc}>
              <div className={styles.name}>
                {validFile ? fileName : file.fileName}
              </div>
              <div className={styles.size}>
                {formatBytes(file.fileSize)}
              </div>
            </div>
            <IconCross className={styles.cross} onClick={handleDeleteFile} />
            <Modal
              title={fileName}
              visible={modal}
              onCancel={e => {
                modalActions.hide();
                e.stopPropagation();
              }}
              footer={null}
            >
              <img
                className={styles.imageFullScreen}
                src={imageUrl}
                alt={file?.fileName}
              />
            </Modal>
          </>
        )}
    </div>
  );
};

export default UploadedFile;
