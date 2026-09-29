import React, { FC } from 'react';
import { Row, Col, Button } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';

import { TransportStatus } from 'api/transport/transport.types';
import { EmptyView } from 'components/EmptyView/EmptyView';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import { useActiveTransport } from 'modules/Vehicles/context/ActiveTransport';
import { useModalForm } from 'modules/Vehicles/context/ModalForm';
import { numberWithSpaces } from 'utils/utils';
import Information from './Information/Information';
import DetailedItem from './DetailedItem/DetailedItem';

import special from 'assets/images/carSpecial.png';
import styles from './TransportDetailed.module.scss';

export const TransportDetailed: FC = () => {
  const { activeTransport, isTransportLoading } = useActiveTransport();
  const { handleOpenEditTransport, handleOpenDeleteTransport } = useModalForm();

  if (isTransportLoading) {
    return <SpinWrapped />;
  }

  if (!activeTransport) {
    return (
      <EmptyView
        title="Автомобиль не выбран"
        description="Выберите автомобиль для получения подробной информации"
      />
    );
  }

  return (
    <div className={styles.content}>
      <div className={styles.header}>
        <div className={styles.imgWrap}>
          <img src={special} alt="car" />
        </div>

        <div className={styles.headerInfo}>
          <div className={styles.regNum}>{activeTransport.stateNumber}</div>

          <Row gutter={[0, 12]}>
            <Col span={12}>
              <DetailedItem title="Марка">{activeTransport.brand || ''}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Модель">{activeTransport.model || ''}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Текущий пробег, км">
                {activeTransport.currentMileage ? numberWithSpaces(activeTransport.currentMileage) : ''}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Год выпуска">{activeTransport.year || ''}</DetailedItem>
            </Col>
          </Row>
        </div>

        {activeTransport.vehicle.status === TransportStatus.IN_USE && (
          <div className={styles.buttonsWrap}>
            <Button
              className={styles.button}
              icon={<EditOutlined />}
              onClick={() => handleOpenEditTransport(activeTransport)}
            />
            <Button
              className={styles.button}
              icon={<DeleteOutlined />}
              onClick={handleOpenDeleteTransport}
            />
          </div>
        )}
      </div>

      <Information
        vehicle={activeTransport.vehicle}
        location={activeTransport.location}
        documents={activeTransport.documents}
        engine={activeTransport.engine}
        general={activeTransport.general}
        service={activeTransport.service}
        contractorId={activeTransport.contractorId}
        autoparkId={activeTransport.autoparkId}
      />
    </div>
  );
};
