import React, { FC, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { FileDoneOutlined, FileExcelOutlined } from '@ant-design/icons';
import { Button, Descriptions, PageHeader } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { PageContent } from 'shared/components/PageContent/PageContent';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { useRequestApprove, useRequestDecline } from 'api/approvals';
import { TripRequestModel } from 'stores/Trip/models';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import { DATE_FORMAT } from 'constants/constants.app';

import ApprovalReasonModal from '../../ApprovalReasonModal/ApprovalReasonModal';
import CompensationDocument from './Compensation';
import { useDescriptionData } from './hooks';

import styles from '../styles.module.scss';

const Content: FC<any> = observer(({ back, request }: { back(): void; request: TripRequestModel }) => {
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { approvalId } = match.params;

  const [requestApprove] = useRequestApprove();
  const [requestDecline] = useRequestDecline();

  const [reasonVisible, setReasonVisible] = useState(false);
  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  function approve(id: string | undefined): void {
    if (id) {
      requestApprove({ id });
      back();
    }
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  // const editHandler = useCallback(() => history.push(`${url}/edit`), [history]);
  // FIXME react-hooks/exhaustive-deps

  // const isPersonalTransport = request.transportType === TransportTypeEnum.PERSONAL;
  const descriptionData = useDescriptionData({ request });

  return (
    <>
      <ApprovalReasonModal
        id={approvalId}
        visible={reasonVisible}
        onOk={(id, reason: IDeclineReason): void => {
          requestDecline({ id, reason }).then(() => {
            toggleReasonModal();
            back();
          });
        }}
        onCancel={toggleReasonModal}
      />
      <PageHeader
        title={`Согласование заявки: ${moment(request.desiredDate).format(
          `${DATE_FORMAT.MONTH_NAME} [в] ${DATE_FORMAT.TIME_SHORT}`
        )}`}
        onBack={back}
      />
      <PageContent>
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
            onClick={(): void => toggleReasonModal()}
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
