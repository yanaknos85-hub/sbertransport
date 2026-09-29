import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';
import { Button, Tooltip } from 'antd';

import { ServiceCard } from 'shared/components/ServiceCard';

import { servicesImage } from '../../constants/services';
import { useTransportTypes } from '../../context/TransportTypes.context';

import ModalAddService from './ModalAddService/ModalAddService';
import { Container } from '../Container/Container';

import styles from './Service.module.scss';

export const Service: FC = () => {
  const { t } = useTranslation();
  const { allActiveTransportTypes } = useTransportTypes();
  const [isModalVisible, setIsModalVisible] = useState(false);

  return (
    <>
      <Container
        title={t.HomePage.service}
        button={(
          <Tooltip title="Только для сервиса пассажирские перевозки">
            <Button className={styles.buttonLinkService}>Перейти в новую версию</Button>
          </Tooltip>
      )}
      >
        <div className={styles.cards}>
          {allActiveTransportTypes.map(transportType => (
            <ServiceCard
              key={transportType.rusName}
              title={transportType.rusName}
              imageSrc={servicesImage[transportType.name]}
            />
          ))}
          <ServiceCard isAddVariant onAdd={() => setIsModalVisible(true)} />
        </div>
      </Container>
      <ModalAddService isVisible={isModalVisible} handleDone={() => setIsModalVisible(false)} />
    </>
  );
};
