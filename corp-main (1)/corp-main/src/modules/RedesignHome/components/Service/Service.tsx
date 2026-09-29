import React, { FC, useState } from 'react';
import { useTranslation } from 'i18n';

import { services, servicesImage } from '../../constants/services';

import { Container } from '../Container/Container';
import { RedesignServiceCard } from 'shared/components/RedesignServiceCard';
import { useTransportTypes } from '../../context/TransportTypes.context';
import ModalAddService from './ModalAddService/ModalAddService';

import styles from './Service.module.scss';

export const Service: FC = () => {
  const { t } = useTranslation();
  const { organizationTransportTypes } = useTransportTypes();
  const [isModalVisible, setIsModalVisible] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [selectedService, setSelectedService] = useState('');

  return (
    <>
      <Container title={t.RedesignHomePage.service}>
        <div className={styles.cards}>
          {services.map((serviceItem, index) => (
            <RedesignServiceCard
              service={serviceItem}
              imageSrc={servicesImage[serviceItem.name]}
              key={index}
              setIsModalVisible={setIsModalVisible}
              setSelectedService={setSelectedService}
              organizationTransportTypes={organizationTransportTypes}
            />
          )
          )}
        </div>
      </Container>
      <ModalAddService
        isVisible={isModalVisible}
        handleDone={() => setIsModalVisible(false)}
        selectedService={selectedService}
        imageSrc={servicesImage[selectedService]}
        organizationTransportTypes={organizationTransportTypes}
      />
    </>
  );
};
