import React, { FC } from 'react';
import { ILogger } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';

import { ITripStore } from 'stores/Trip/Trip.interface';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import uuid from 'utils/uuid';

import { FileStatus, getBase64 } from '../../additional/fileUtils/fileUploadUtils';
import { tripsInfoTitle } from '../../constants';
import { SuburbFormItem } from './SuburbFormItem';

interface WithFileProps {
  form: FormInstance;
  field: FormListFieldData;
  tripStore: ITripStore;
  logger: ILogger;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
}
export const WithFile: FC<WithFileProps> = observer(
  ({
    form, field, tripStore, logger, updateGeneralSum, currentTransportTypes,
  }) => {
    const folder = uuid();
    const [imageUrl, setImageUrl] = React.useState<string>();

    const [loading, setLoading] = React.useState<boolean>(false);
    const [file, setFile] = React.useState<File>();

    const handleLoadFileChange = (info: any): void => {
      if (info.file.status === FileStatus.UPLOADING) {
        setLoading(true);
        return;
      }
      if (info.file.status === FileStatus.DONE) {
        setFile(info.file);
        tripStore.saveFile(info.file.originFileObj, folder).then(data => {
          const tripsInfo = form.getFieldValue(tripsInfoTitle);
          const trip = tripsInfo[field.key];
          trip.savedFileData = data;
          logger.toMessage('info', SYSTEM_MESSAGES.fileUploadSuccess);
        });
        // eslint-disable-next-line no-shadow
        getBase64(info.file.originFileObj, (imageUrl: any) => {
          // FIXME no-shadow
          setLoading(false);
          setImageUrl(imageUrl);
        });
      }
    };

    return (
      <SuburbFormItem
        field={field}
        logger={logger}
        file={file}
        loading={loading}
        imageUrl={imageUrl}
        setImageUrl={setImageUrl}
        updateGeneralSum={updateGeneralSum}
        handleLoadFileChange={handleLoadFileChange}
        currentTransportTypes={currentTransportTypes}
      />
    );
  }
);
