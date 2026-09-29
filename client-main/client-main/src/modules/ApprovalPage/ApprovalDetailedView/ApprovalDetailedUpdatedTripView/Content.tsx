import { FileDoneOutlined, FileExcelOutlined } from '@ant-design/icons';
import { Button, Descriptions, PageHeader } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { useState } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import MapComponent from 'shared/components/Map/Map';
import { PageContent } from 'shared/components/PageContent/PageContent';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useTripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TripRequestModel } from 'stores/Trip/models';

import ApprovalReasonModal from '../../ApprovalReasonModal/ApprovalReasonModal';

import styles from '../styles.module.scss';
import { useDescriptionData, useUpdatedTripHandlers } from './hooks';
import { useApprovalId } from './hooks/approval';

const Content = observer(
  ({
    request,
    updatedRequest,
    back,
  }: {
    request: TripRequestModel;
    updatedRequest?: TripRequestModel;
    back(): void;
  }) => {
    const {
      approvalId, approvalIdIsLoading, refetchApprovalId,
    } = useApprovalId(request);

    const [reasonVisible, setReasonVisible] = useState(false);
    const toggleReasonModal = (): void => setReasonVisible(x => !x);

    // Отвечает за состояние основной заявки
    const tripRequestRoute = useTripRequestRoute(request);
    const { actualRoute, inProgress: routeIsLoading } = tripRequestRoute;

    // При редактировании согласованной заявки на бэке создаётся её клон.
    // Этот хук отвечает за её состояние
    const updatedTripRequestRoute = useTripRequestRoute(updatedRequest);
    const { inProgress: updatedRouteIsLoading } = updatedTripRequestRoute;

    const onChangeRequest = () => {
      refetchApprovalId();
    };
    const descriptionData = useDescriptionData({
      tripRequestRoute,
      updatedTripRequestRoute,
      onChangeRequest,
    });

    const { handleApprove, handleDecline } = useUpdatedTripHandlers({
      approvalId, back, toggleReasonModal,
    });

    const buttonIsDisabled = routeIsLoading || updatedRouteIsLoading || approvalIdIsLoading;

    return (
      <>
        {approvalId && (
          <ApprovalReasonModal
            id={approvalId}
            visible={reasonVisible}
            onOk={handleDecline}
            onCancel={toggleReasonModal}
          />
        )}
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
          <div className={styles.buttonBlock}>
            <Button
              disabled={buttonIsDisabled}
              onClick={handleApprove}
              type="primary"
              icon={<FileDoneOutlined />}
            >
              Согласовать
            </Button>
            <Button
              disabled={buttonIsDisabled}
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
  }
);

export default withErrorBoundary(Content);
