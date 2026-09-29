import React, { FC, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Spin } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { useProfile } from 'api/profile';
import { useDeleteTripPurpose, useActiveTripPurposes } from 'api/purposes';
import { Button } from 'shared/components/Button/Button';
import { TableSpacingRows } from 'shared/components/Tables/TableSpacingRows/TableSpacingRows';
import { useModalState } from 'shared/hooks/useModal';
import { Modal } from 'shared/components/Modal/Modal';
import { useTranslation } from 'i18n';
import { useColumns } from '../hooks/useColumns';
import { processIncomingJsonArray } from '../utils/utils';
import TripPurposeContainer from './TripPurposeContainer';
import { TripPurposesInitForm } from './TripPurposesInitForm';

import styles from './TripPurposes.module.scss';

export const TripPurposesHandbookComponent: FC = () => {
  const {
    t: {
      global,
      SettingsTripRules: {
        tripPurposes,
        tripPurposes: { updatePurposeForm },
      },
    },
  } = useTranslation();
  const match = useRouteMatch();

  const { organizationId } = useProfile().data;
  // @ts-ignore
  const { data: purposes, isFetching } = useActiveTripPurposes(organizationId);
  const [deleteTripPurpose] = useDeleteTripPurpose();
  const [visible, {
    hide, toggle, show,
  }] = useModalState();
  const [purposeId, setPurposeId] = useState<string>('adding');

  const handleEdit = (id: string): void => {
    setPurposeId(id);
    show();
  };

  const handleClose = (): void => {
    setPurposeId('');
    hide();
  };

  const handleAddNewClick = (): void => {
    setPurposeId('adding');
    show();
  };

  const deleteButton = {
    fixed: 'right',
    width: 72,
    render: (_: any, record: any): JSX.Element => (
      <TableEditButtons
        path={match.path}
        onEdit={() => handleEdit(record.id)}
        id={record.id}
        // @ts-ignore
        onDelete={() => deleteTripPurpose({ orgId: organizationId, purId: record.id })}
        cancelText={global.cancel}
        okText={global.delete}
        title={tripPurposes.deletePurposeConfirmation}
      />
    ),
  };

  const renderFooter = (): JSX.Element => (
    <div className={styles.ButtonBlock}>
      <Button
        icon={<PlusOutlined />}
        size="small"
        onClick={handleAddNewClick}
        className={styles.addNewPurposeButton}
      >
        {tripPurposes.initFormCreateButtonCaption}
      </Button>

      {/*
      скрыто до уточнения макета

      <DownloadButton
        url={`${importExportEndpointMap.tripPurpose}/files/tripPurpose`}
        className={styles.DownloadButton}
        fileName='цели_поездок'
      />

      <UploadButton entity="tripPurpose" useUpload={useUploadTripPurposes} /> */}
    </div>
  );

  const columns = useColumns(deleteButton);

  return (
    <div>
      <Spin spinning={isFetching}>
        {purposes.purposes.length ? (
          <TableSpacingRows
            columns={columns}
            dataSource={processIncomingJsonArray(purposes.purposes)}
            scroll={{ x: 600 }}
            rowKey="id"
            size="small"
            tableLayout="auto"
            footer={renderFooter}
            className={styles.tripPurposesRows}
          />
        ) : (
          <TripPurposesInitForm onCreate={handleAddNewClick} />
        )}
      </Spin>

      <Modal
        onCancel={handleClose}
        visible={visible}
        footer={null}
      >
        <TripPurposeContainer
          handleClose={handleClose}
          id={purposeId}
          key={purposeId}
        />
      </Modal>
    </div>
  );
};

export default TripPurposesHandbookComponent;
