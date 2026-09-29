import './override.scss';
// import {IS_REMOTE} from "constants/constants.env";

// if (!IS_REMOTE) {
//   import('antd/dist/antd.css');  // для девелопа! если микро запущен как главный контейнер
// }

import { Button, Descriptions, Form } from 'antd';
import classNames from 'classnames';
import React, { useState } from 'react';

import QRCode from 'react-qr-code';

import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';

import { MapComponent } from 'shared/components/Map/MapComponent';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';

import { AddingFileComponent } from './AddingFileComponent';
import { usePersonalTrip } from './hooks/personal';
import { RateModal } from './RateModal/RateModal';
import { useDescriptionData } from './utils';
import WayPoints from './WayPoints';
import { useCarsharingTrip } from './hooks/useCarsharingTrip';

import styles from './styles.module.scss';
import { TModal } from 'shared/ui/Modal/Modal';

import { useGetCarsharingDeepLink } from 'api/requests';
import Process from '../../Evaluation/Constants/Process';
import { Delegate } from 'stores/Delegates/Delegates.interface';

const TripRequestContentOld = ({
  request,
  delegatesList,
  tripFromCoop,
}: {
  request: TripRequestModel;
  delegatesList: Delegate[];
  tripFromCoop?: TripFromCoopModel;
  goToPlanned: () => void;
}): JSX.Element => {
  const tripRequestRoute = useTripRequestRoute(request);
  const { actualRoute } = tripRequestRoute;
  const delegateListString = delegatesList?.map((delegate: Delegate) => {
    const {
      lastName, firstName, patronymic,
    } = delegate.delegateEmployee;
    return `${lastName} ${firstName.charAt(0)}. ${patronymic?.charAt(0)}. `;
  });
  const delegates = delegateListString?.length ? delegateListString : 'Список делегатов пуст';
  const {
    timeline, descriptionFields, activeStatus,
  } = useDescriptionData({
    request, delegates, tripFromCoop,
  });
  const [, setShowModal] = useState(false);
  const [process, setProcess] = useState<Process>(Process.CLOSE);

  const {
    isPersonalTransport,
    personalTripInProgress,
    personalRequestIsApproved,
    isNotSharedOwner,
    startPersonalTrip,
  } = usePersonalTrip({ request, tripFromCoop });

  const {
    isCarsharingTransport,
    carsharingTripInProgress,
    carsharingRequestIsApproved,
    startCarsharingTrip,
  } = useCarsharingTrip(request);

  const deepLink = useGetCarsharingDeepLink(request.id);
  const [form] = Form.useForm();

  const handleCarsharingOpen = (e: React.MouseEvent) => {
    setProcess(Process.START);
    e.stopPropagation();
  };

  const handleCarsharingConfirm = () => {
    setProcess(Process.CLOSE);
    return Promise.resolve(200);
  };

  const handleCarsharingClose = () => {
    setProcess(Process.CLOSE);
  };

  return (
    <Form form={form}>
      <MapComponent
        markers={actualRoute.waypoints}
        polylines={actualRoute.segments}
        className={styles.map}
        dragging
        zoomControl
      />
      <WayPoints tripRequestRoute={tripRequestRoute} />
      <div className={styles.fieldsContainer}>
        <div className={styles.timeline}>
          <Descriptions
            size="small"
            className={classNames(styles.detailsList)}
            column={3}
          >
            <Descriptions.Item
              key={`${timeline.label}`}
              label={timeline.label}
              span={3}
            >
              {timeline.steps}
            </Descriptions.Item>
          </Descriptions>
        </div>
        <div className={styles.descriptionFields}>
          <Descriptions
            size="small"
            className={classNames(styles.detailsList)}
            column={3}
          >
            {descriptionFields.map((descriptionItem, index) => (
              <Descriptions.Item
                key={`${descriptionItem[0]}-${index + 1}`}
                label={descriptionItem[0]}
                span={3}
              >
                {descriptionItem[1]}
              </Descriptions.Item>
            ))}
          </Descriptions>
        </div>
      </div>

      {request.isFinished && !request.isRated && <RateModal request={request} setShowModal={setShowModal} />}

      {request.status === 'PUBLIC_TRIP_CONFIRMATION' && <AddingFileComponent request={request} />}
      {isCarsharingTransport && (
        <Button
          onClick={handleCarsharingOpen}
          block
          className={styles.btnSettings}
        >
          Продолжить в мобильном приложении
        </Button>
      )}
      {personalRequestIsApproved && isPersonalTransport && isNotSharedOwner && (
        <Button
          onClick={startPersonalTrip}
          block
          className={styles.btnSettings}
        >
          Начать поездку
        </Button>
      )}
      {carsharingRequestIsApproved && isCarsharingTransport && (
        <Button
          onClick={startCarsharingTrip}
          block
          className={styles.btnSettings}
        >
          Начать поездку
        </Button>
      )}
      {(personalTripInProgress || carsharingTripInProgress) && activeStatus?.cancelable && (
        <Button
          block
          className={styles.btnSettings}
          htmlType="submit"
        >
          Завершить поездку
        </Button>
      )}

      <TModal
        properties={{
          title: 'Отсканируйте QR-код',
          confirmButton: { text: 'Готово' },
          declineButton: { text: 'Отменить', hide: true },
          handleConfirm: handleCarsharingConfirm,
          style: {
            width: 300,
            height: 511,
            justifyContent: 'center',
            textAlign: 'center',
            margin: '30px 0 0 0',
            padding: '0 24px 24px 24px',
          },
          withoutScrolls: true,
        }}
        process={process}
        onClose={handleCarsharingClose}
        isPoppup={false}
        isReadOnly={false}
      >
        <div className={styles.modalContainer}>
          <p className={styles.modalContainer__title}>
            Для начала поездки необходимо отсканировать QR-код на вашем смартфоне
          </p>
          <QRCode style={{ width: '100%' }} value={deepLink.data.deepLink} />
        </div>
      </TModal>
    </Form>
  );
};

export default TripRequestContentOld;
