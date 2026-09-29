import React, { CSSProperties, FC } from 'react';
import {
  FileStatus,
  customRequest
} from 'modules/EmployeeApp/components/TaxiClasses/Card/PublicTransportCompensationsModal/Components/additional/fileUtils/fileUploadUtils';
import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { Upload } from './SRMMassUploadMultiple.style';

interface Props {
  style?: CSSProperties;
  onUpload: () => void;
  setVisible?: (visible: boolean) => void;
}

const SRMMassUploadMultiple: FC<Props> = props => {
  const { onUpload, setVisible } = props;

  const {
    [StoreNames.srmMassStoreMultiple]: srmMassStoreMultiple, logger,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const { selfEmployee } = selfStore;

  const validateFile = async ({ originFileObj }: any) => {
    try {
      await srmMassStoreMultiple.validateFile(originFileObj, selfEmployee.id);
      onUpload();
    } catch (err: any) {
      logger.toMessage('error', err.message);
    }
  };

  const handleLoadFileChange = ({ file }: any): void => {
    if (file.status === FileStatus.UPLOADING) {
      return;
    }
    if (file.status === FileStatus.DONE) {
      validateFile(file);
      setVisible?.(false);
    }
  };

  return (
    <div>
      <Upload
        showUploadList={false}
        customRequest={customRequest}
        onChange={handleLoadFileChange}
        style={{ width: '100%' }}
      >
        <p>
          <span className="text">Выберите файлы</span>
          {' '}
          или перетяните их сюда
        </p>
      </Upload>
    </div>
  );
};

export default SRMMassUploadMultiple;
