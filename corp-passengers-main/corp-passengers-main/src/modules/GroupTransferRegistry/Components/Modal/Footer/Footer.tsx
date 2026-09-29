import { Button, Upload } from 'antd';
import { RcFile } from 'antd/lib/upload';
import { useTranslation } from 'i18n';
import React, { FC } from 'react';
import { FILE_FORMAT } from 'constants/constants.app';
import { UploadFile } from 'antd/lib/upload/interface';
import { DataForm } from 'api/reports';
import styles from '../Modal.module.scss';
import { UploadResultButton } from './UploadResultButton ';

interface OwnProps {
  uploadStatus: { success: boolean };
  status: DataForm | null;
  fileList: RcFile[];
  setFileList: (fileList: RcFile[]) => void;
  handlerClick: () => void;
  uploadButtonDisable: boolean;
  handlerCancel: () => void;
}

export const Footer: FC<OwnProps> = ({
  uploadStatus: { success },
  status,
  fileList,
  setFileList,
  handlerClick,
  uploadButtonDisable,
  handlerCancel,
}): JSX.Element => {
  const { XLS, XLSX } = FILE_FORMAT;
  const { t } = useTranslation();
  const props = {
    onRemove: (file: UploadFile) => {
      const index = fileList.indexOf(file as RcFile);
      const newFileList = fileList.slice();
      newFileList.splice(index, 1);
      setFileList(newFileList);
    },
    beforeUpload: (file: RcFile) => {
      setFileList([...fileList, file]);
      return false;
    },
    fileList,
  };
  return !success ? (
    <div className={fileList.length > 0 ? styles.downloadFooter : styles.modalFooter}>
      <Upload
        className={styles.uploadButton}
        {...props}
        accept={`${XLSX},${XLS}`}
      >
        {!fileList.length && <Button>{t.global.selectTheFile}</Button>}
      </Upload>
      <div className={styles.buttonUpload}>
        {!!fileList.length && (
          <Button disabled={uploadButtonDisable} onClick={handlerClick}>
            {status ? t.global.replace : t.global.download}
          </Button>
        )}
        <Button className={styles.buttonCancel} onClick={handlerCancel}>
          {t.global.cancel}
        </Button>
      </div>
    </div>
  ) : (
    <UploadResultButton handlerCancel={handlerCancel} />
  );
};
