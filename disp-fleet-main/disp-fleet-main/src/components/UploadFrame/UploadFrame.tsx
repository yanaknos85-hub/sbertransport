import React, {
  ChangeEvent, Dispatch, FC, SetStateAction
} from 'react';
import { CloseOutlined } from '@ant-design/icons';
import cn from 'classnames';

import { LoadingProgress } from 'components/LoadingProgress/LoadingProgress';
import { ReactComponent as ExcelFileIcon } from 'assets/icons/excel-file.svg';
import styles from './UploadFrame.module.scss';

interface UploadFrameProps {
  file: File | undefined;
  setFile: Dispatch<SetStateAction<File | undefined>>;
  accept?: React.InputHTMLAttributes<File>['accept'];
  progress?: number;
  isLoading?: boolean;
  hasError?: boolean;
  disabled?: boolean;
}

const UploadFrame: FC<UploadFrameProps> = ({
  file,
  setFile,
  accept = '.xls,.xlsx,.XLS,.XLSX',
  progress,
  isLoading = false,
  disabled = false,
  hasError = false,
}) => {
  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      const file = event.target.files[0];

      if (accept && !accept.split(',').some(format => file.name.includes(format))) return;

      setFile(file);
    }
  };

  const handleClose = () => {
    setFile(undefined);
  };

  return file ? (
    <div className={styles.fileDroppedContainer}>
      <div className={styles.selectedFile}>
        <div className={styles.flex}>
          <ExcelFileIcon />
          <div className={styles.fileData}>
            <span
              className={cn(styles.fileName, {
                [styles.redSpan]: hasError,
              })}
            >
              {file.name}
            </span>

            {isLoading ? (
              <LoadingProgress progress={progress ?? 0} />
            ) : (
              <span className={styles.fileSize}>
                {(file.size / 1024).toFixed(2)}
                &nbsp;Кб
              </span>
            )}
          </div>
        </div>
        <CloseOutlined onClick={handleClose} />
      </div>

      {hasError && (
        <span className={styles.redSpan}>
          Есть критичные совпадения, необходимо внести изменения в файл и загрузить повторно.
          <br />
          Расхождения по записям в выгруженном файле.
        </span>
      )}
    </div>
  ) : (
    <>
      <div
        className={cn(styles.dropFileContainer, {
          [styles.dropDisabled]: disabled,
        })}
      >
        <input
          id="fileInput"
          type="file"
          accept={accept}
          onChange={handleFileChange}
          className={styles.uploadInput}
          disabled={disabled}
        />
        <span className={styles.chooseFileText}>Выберите файл</span>
        &nbsp;
        <span>или перетяните его сюда</span>
      </div>
    </>
  );
};

export default UploadFrame;
