import React from 'react';
import type { FC } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Divider, Row, Col } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import moment from 'moment';

import { useTranslation } from 'i18n';
import * as routes from 'constants/routes.constants';
import { DATE_FORMAT } from 'constants/app.constants';
import { CancelWaybillStatuses, WaybillStatus } from 'api/waybill/waybill.constants';
import { Waybill } from 'api/waybill/waybill.types';
import type { UUID } from 'utils/io-ts';
import { useModalState } from 'hooks/useModal';
import Panel from 'components/Panel/Panel';
import { Button } from 'components/Button';

import {
  Transportation,
  TransportationInfo,
  Communication,
  CommunicationInfo
} from 'modules/Waybill/Waybill.constants';
import StatusField from '../StatusField/StatusField';
import CloseModal from './CloseModal/CloseModal';
import CancelModal from './CancelModal/CancelModal';
import FieldBlock from '../FieldBlock';

import styles from './Header.module.scss';

interface Props {
  waybill: Waybill;
  humanReadableId: string;
  creationDate: string;
  startDate: string;
  finishDate: string;
  ewbUuid: UUID;
  status: WaybillStatus;
}

const enum ModalType {
  Cancel = 'cancel',
  Close = 'close',
}

const DetailedHeader: FC<Props> = ({
  waybill,
  creationDate,
  startDate,
  finishDate,
  ewbUuid,
  status,
}) => {
  const history = useHistory();
  const { header: i18 } = useTranslation().t.Waybill.detailed;

  const [modalVisible, { show: showModal, hide: hideModal }] = useModalState();
  const modalTypeAccess: ModalType | null = CancelWaybillStatuses.includes(status)
    ? ModalType.Cancel : status === WaybillStatus.ON_THE_LINE
      ? ModalType.Close : null;

  return (
    <Panel className={styles.panel}>
      <div className={styles.panel__header}>
        <div className={styles.title}>
          <ArrowLeftOutlined onClick={() => history.push(routes.RELEASE_ON_LINE_LINK)} />
          <span>{`${i18.title} ${waybill.humanReadableId}`}</span>
        </div>

        {modalTypeAccess && (
          <Button
            type="primary"
            className={styles.button}
            onClick={showModal}
          >
            {modalTypeAccess === ModalType.Close ? i18.closeButton : i18.cancelButton}
          </Button>
        )}
      </div>

      <Divider className={styles.panel__divider} />

      <Row gutter={[10, 16]}>
        <Col span={6}>
          <FieldBlock
            title={i18.fields.humanReadableId}
            value={waybill.humanReadableId}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.creationDate}
            value={moment(creationDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.ewbUuid}
            value={ewbUuid}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.status}
            value={<StatusField status={status} />}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.startDate}
            value={moment(startDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.finishDate}
            value={moment(finishDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.transportationType}
            value={TransportationInfo[Transportation.OwnNeeds]}
          />
        </Col>

        <Col span={6}>
          <FieldBlock
            title={i18.fields.communicationType}
            value={CommunicationInfo[Communication.Urban]}
          />
        </Col>
      </Row>

      <CloseModal
        visible={modalTypeAccess === ModalType.Close && modalVisible}
        waybill={waybill}
        onClose={hideModal}
      />

      <CancelModal
        visible={modalTypeAccess === ModalType.Cancel && modalVisible}
        id={waybill.id}
        humanReadableId={waybill.humanReadableId}
        onClose={hideModal}
      />
    </Panel>
  );
};

export default DetailedHeader;
