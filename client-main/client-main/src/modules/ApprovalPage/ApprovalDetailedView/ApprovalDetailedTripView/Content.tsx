import { FileDoneOutlined, FileExcelOutlined } from '@ant-design/icons';
import { Button, Descriptions, PageHeader } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { useState } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useFinalTripApprove, useFinalTripDecline } from 'api/approvals';

import { DATE_FORMAT } from 'constants/constants.app';

import MapComponent from 'shared/components/Map/Map';
import { PageContent } from 'shared/components/PageContent/PageContent';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { useUpdateTripRequest } from 'shared/hooks/trip/useUpdateTripRequest';
import { TripRequestModel } from 'stores/Trip/models';
import { IDeclineReason } from 'stores/Trip/Trip.interface';

import ApprovalReasonModal from '../../ApprovalReasonModal/ApprovalReasonModal';
import CompensationDocument from '../ApprovalDetailedRequestView/Compensation';

import styles from '../styles.module.scss';
import { useDescriptionData } from './hooks';

const Content = observer(({ request, back }: { request: TripRequestModel; back(): void }) => {
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { approvalId } = match.params;

  const tripRequestRoute = useTripRequestRoute(request);
  const { updateTripRequest } = useUpdateTripRequest({ request, tripRequestRoute });
  const { actualRoute, geoWaypoints } = tripRequestRoute;

  const [finalTripApprove] = useFinalTripApprove();
  const [finalTripDecline] = useFinalTripDecline();

  const [reasonVisible, setReasonVisible] = useState(false);
  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  // const editHandler = useCallback(() => history.push(`${url}/edit`), [history]);
  // FIXME react-hooks/exhaustive-deps

  const descriptionData = useDescriptionData({
    request,
    geoWaypoints,
    actualRoute,
    updateTripRequest,
  });

  return (
    <>
      <ApprovalReasonModal
        id={approvalId}
        visible={reasonVisible}
        onOk={(id, reason: IDeclineReason): void => {
          finalTripDecline({ id, reason }).then(() => {
            toggleReasonModal();
            back();
          });
        }}
        onCancel={toggleReasonModal}
      />
      <PageHeader
        title={`Утверждение маршрута: ${moment(request.desiredDate).format(
          `${DATE_FORMAT.MONTH_NAME} [в] ${DATE_FORMAT.TIME_SHORT}`
        )}`}
        onBack={back}
      />
      <PageContent>
        <MapComponent
          markers={actualRoute.waypoints}
          polylines={actualRoute.segments}
          className={styles.map}
        />
        <Descriptions
          size="small"
          className={styles.detailsList}
          bordered={true}
          column={3}
        >
          {descriptionData.map((descriptionItem, index) => (
            <Descriptions.Item
              key={`${descriptionItem[0]}-${index + 1}`}
              label={descriptionItem[0]}
              span={3}
            >
              {descriptionItem[1]}
            </Descriptions.Item>
          ))}
        </Descriptions>
        <CompensationDocument request={request} />
        {/* FIXME отключено в рамках TRANSPORT-3872 */}
        {/* <Button onClick={editHandler} block icon={<EditOutlined />} className={styles.buttonEdit}>
          Редактировать
        </Button> */}
        <div className={styles.buttonBlock}>
          <Button
            onClick={(): void => {
              finalTripApprove({ id: approvalId }).then(() => {
                back();
              });
            }}
            type="primary"
            icon={<FileDoneOutlined />}
          >
            Согласовать
          </Button>
          <Button
            onClick={toggleReasonModal}
            danger={true}
            icon={<FileExcelOutlined />}
          >
            Отклонить
          </Button>
        </div>
      </PageContent>
    </>
  );
});

export default withErrorBoundary(Content);
