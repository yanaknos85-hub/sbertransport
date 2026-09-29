import React, { CSSProperties, FC } from 'react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import {
  customRequest,
  FileStatus
} from 'modules/TaxiClasses/Card/PublicTransportCompensationsModal/Components/additional/fileUtils/fileUploadUtils';

import { Upload } from './CaroMassUploadMultiple.style';

interface Props {
  style?: CSSProperties;
  onUpload: () => void;
  setVisible?: (visible: boolean) => void;
}

const CargoMassUploadMultiple: FC<Props> = props => {
  const { onUpload, setVisible } = props;

  const {
    [StoreNames.cargoMassStoreMultiple]: cargoMassStoreMultiple,
    [StoreNames.cargoStore]: cargoStore,
    logger,
  } = useAppStoreContext();

  const saveFile = async ({ originFileObj }: any) => {
    try {
      if (cargoStore.isRegular) {
        await cargoMassStoreMultiple.saveFileRegular(originFileObj);
      } else {
        await cargoMassStoreMultiple.saveFile(originFileObj);
      }
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
      saveFile(file);
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

export default CargoMassUploadMultiple;
