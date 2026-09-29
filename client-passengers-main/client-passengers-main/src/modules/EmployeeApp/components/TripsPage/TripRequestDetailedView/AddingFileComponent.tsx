import { Button } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useChangeRequestStatus } from 'api/trip-requests';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models/TripRequest.model';
import { SavedFileInfo } from 'stores/Trip/Trip.interface';

import { useCurrentTripRequest } from 'shared/hooks/trip';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { UUID } from 'utils/io-ts';
import uuid from 'utils/uuid';

import { FileUploadFormItem } from '../../TaxiClasses/Card/PublicTransportCompensationsModal/Components/additional/fileUtils/FileUploadFormItem';
import {
  FileStatus,
  getBase64
} from '../../TaxiClasses/Card/PublicTransportCompensationsModal/Components/additional/fileUtils/fileUploadUtils';

import styles from './styles.module.scss';

export const AddingFileComponent: FC<{ request: TripRequestModel }> = observer(({ request }) => {
  const [imageUrl, setImageUrl] = React.useState<string>();
  const { [StoreNames.tripStore]: tripStore, logger } = useAppStoreContext();
  const [loading, setLoading] = React.useState<boolean>(false);
  const [file, setFile] = React.useState<File>();
  const folder = uuid();
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;
  const [changeRequestStatus] = useChangeRequestStatus();
  const { refetchRequestTrip } = useCurrentTripRequest();
  const [info, setInfo] = React.useState<any>();

  const updateRequestStatus = (result: number) => {
    if (result === 200) {
      changeRequestStatus({
        reqId,
        reqStatus: 'PUBLIC_AWAITING_AFFIRMATIVE',
      }).then(() => {
        refetchRequestTrip();
      });
    }
  };

  const saveConfirmFile = () => {
    tripStore.saveFile(info.file.originFileObj, folder).then((data: SavedFileInfo) => {
      if (request?.transportCompensation?.[0]?.transportType?.publicCompensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION) {
        tripStore
          .saveConfirmSuburbTripFile(request.id, [data])
          .then(result => updateRequestStatus(result))
          .catch(error => logger.toError('error', error?.response?.message || error.message));
      } else if (request?.transportCompensation?.[0]?.transportType?.publicCompensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION) {
        tripStore
          .saveConfirmCardTripFile(request.id, [data])
          .then(result => updateRequestStatus(result))
          .catch(error => logger.toError('error', error?.response?.message || error.message));
      }
    });
  };

  const handleLoadFileChange = (savedInfo: any): void => {
    if (savedInfo.file.status === FileStatus.UPLOADING) {
      setLoading(true);
      return;
    }
    if (savedInfo.file.status === FileStatus.DONE) {
      setFile(savedInfo.file);
      setInfo(savedInfo);

      getBase64(savedInfo.file.originFileObj, (newImageUrl: any) => {
        setLoading(false);
        setImageUrl(newImageUrl);
      });
    }
  };

  return (
    <>
      <FileUploadFormItem
        title="Документ, подтверждающий покупку билетов"
        logger={logger}
        file={file}
        loading={loading}
        imageUrl={imageUrl}
        setImageUrl={setImageUrl}
        handleLoadFileChange={handleLoadFileChange}
      />

      <Button
        onClick={saveConfirmFile}
        block
        className={styles.btnSettings}
      >
        Подтвердить
      </Button>
    </>
  );
});
