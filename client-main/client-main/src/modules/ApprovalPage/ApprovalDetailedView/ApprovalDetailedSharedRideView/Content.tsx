import { FileDoneOutlined, FileExcelOutlined } from '@ant-design/icons';
import { Button, Descriptions, PageHeader } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { FC } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useSharedRideRequestApprove, useSharedRideRequestDecline } from 'api/approvals';

import { DATE_FORMAT } from 'constants/constants.app';

import MapComponent from 'shared/components/Map/Map';
import { PageContent } from 'shared/components/PageContent/PageContent';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useTripRequestRoute } from 'shared/hooks/trip';
import { TripRequestModel } from 'stores/Trip/models';

import styles from '../styles.module.scss';
import { useDescriptionData } from './hooks';

interface ContentProps {
  request?: TripRequestModel;
  back(): void;
}

const Content: FC<ContentProps> = observer(({ back, request }: ContentProps) => {
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { approvalId } = match.params;

  const [sharedRideApprove] = useSharedRideRequestApprove();
  const [sharedRideDecline] = useSharedRideRequestDecline();

  const approve = (id?: string): void => {
    if (id) {
      sharedRideApprove({ id }).then(() => {
        back();
      });
    }
  };

  const decline = (id?: string): void => {
    if (id) {
      sharedRideDecline({ id }).then(() => {
        back();
      });
    }
  };

  // const editHandler = useCallback(() => history.push(`${url}/edit`), [history, url]);
  // const isPersonalTransport = request?.transportType === TransportTypeEnum.PERSONAL;

  const tripRequestRoute = useTripRequestRoute(request);
  const { actualRoute } = tripRequestRoute;

  const descriptionData = request
    ? useDescriptionData({
      request,
    })
    : [];

  return (
    <>
      <PageHeader
        title={`Согласование заявки: ${moment(request?.desiredDate).format(
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
        {/* FIXME отключено в рамках TRANSPORT-3872 */}
        {/* {isPersonalTransport && (
          <Button onClick={editHandler} block icon={<EditOutlined />} className={styles.buttonEdit}>
            Редактировать
          </Button>
        )} */}
        <div className={styles.buttonBlock}>
          <Button
            onClick={(): void => approve(approvalId)}
            type="primary"
            icon={<FileDoneOutlined />}
          >
            Согласовать
          </Button>
          <Button
            onClick={(): void => decline(approvalId)}
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
