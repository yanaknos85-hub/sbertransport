/* eslint-disable @typescript-eslint/no-explicit-any */
import { ILogger } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useEffect
} from 'react';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { ITripStore } from 'stores/Trip/Trip.interface';

import uuid from 'utils/uuid';

import { getBase64 } from '../../additional/fileUtils/fileUploadUtils';
import { tripsInfoTitle } from '../../constants';
import { SuburbFormItem } from './SuburbFormItem';
import { PublicApprovals } from 'api/trip-requests';
import { PaidServicesFormItem } from './PaidServicesFormItem';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { FileCompensation, FileStatus } from 'modules/EmployeeApp/components/CreateTripRequest/types/types';

interface WithFileProps {
  form: FormInstance;
  field: FormListFieldData;
  tripStore: ITripStore;
  logger: ILogger;
  updateGeneralSum: () => void;
  currentTransportTypes: LabeledValue[];
  publicApprovals: PublicApprovals | null;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  index: number;
  compensationType: string;
}
export const WithFile: FC<WithFileProps> = observer(
  ({
    form,
    field,
    tripStore,
    logger,
    updateGeneralSum,
    currentTransportTypes,
    publicApprovals,
    setIsSaveCompensation,
    index,
    compensationType,
  }) => {
    const folder = uuid();
    const [imageUrl, setImageUrl] = React.useState<string | ArrayBuffer | null>();

    const [loading, setLoading] = React.useState<boolean>(false);
    const [file, setFile] = React.useState<FileCompensation>();

    useEffect(() => {
      setFile(form.getFieldValue(tripsInfoTitle)[index].ticket?.file);
    }, []);

    const handleLoadFileChange = (fileCompensation: FileCompensation): void => {
      if (fileCompensation.status === FileStatus.UPLOADING) {
        setLoading(true);
        return;
      }
      if (fileCompensation.status === FileStatus.DONE) {
        setFile(fileCompensation);
        tripStore.saveFile(fileCompensation.originFileObj, folder).then(data => {
          const tripsInfo = form.getFieldValue(tripsInfoTitle);
          const trip = tripsInfo[field.key];
          trip.savedFileData = data;
          tripStore.setIsSaveFile(false);
          logger.toMessage('info', SYSTEM_MESSAGES.fileUploadSuccess);
        })
          .catch(() => {
            tripStore.setIsSaveFile(true);
            logger.toMessage('error', SYSTEM_MESSAGES.fileUploadError);
          });
        getBase64(fileCompensation.originFileObj, (imgUrl: string | ArrayBuffer | null) => {
          setLoading(false);
          setImageUrl(imgUrl);
        });
      }
    };

    const getSuburbItem = (): JSX.Element => (
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
        form={form}
        publicApprovals={publicApprovals}
        setIsSaveCompensation={setIsSaveCompensation}
        index={index}
        setFile={setFile}
      />
    );

    const getPaidServicesItem = (): JSX.Element => (
      <PaidServicesFormItem
        field={field}
        logger={logger}
        file={file}
        loading={loading}
        imageUrl={imageUrl}
        setImageUrl={setImageUrl}
        updateGeneralSum={updateGeneralSum}
        handleLoadFileChange={handleLoadFileChange}
        currentTransportTypes={currentTransportTypes}
        form={form}
        publicApprovals={publicApprovals}
        setIsSaveCompensation={setIsSaveCompensation}
        index={index}
        setFile={setFile}
      />
    );

    const getItem = (): JSX.Element => {
      const isSuburbTrip = compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION;
      return isSuburbTrip ? getSuburbItem() : getPaidServicesItem();
    };

    return <>{getItem()}</>;
  }
);
