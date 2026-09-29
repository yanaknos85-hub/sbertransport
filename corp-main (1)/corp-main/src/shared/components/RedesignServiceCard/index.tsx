import React, { FC, useEffect, useState } from 'react';

import addIcon from 'shared/images/addIcon.svg';
import { OrganizationTransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { ITypesService } from 'modules/RedesignHome/types/Home.types';
import { typesServiceCargo, typesServiceCarService, typesServicePassengers } from 'modules/RedesignHome/constants/services';

import styles from './RedesignServiceCard.module.scss';

interface IService {
  name: string;
  title: string;
  numberConnectedServices: number;
  maxServices: number;
}

interface Props {
  service: IService;
  imageSrc?: string;
  setIsModalVisible: React.Dispatch<React.SetStateAction<boolean>>;
  setSelectedService: React.Dispatch<React.SetStateAction<string>>;
  organizationTransportTypes: OrganizationTransportTypes[];
}

export const RedesignServiceCard: FC<Props> = ({
  service,
  imageSrc,
  setIsModalVisible,
  setSelectedService,
  organizationTransportTypes,
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const handleLoad = () => setIsLoaded(true);
  const [typesService, setTypesService] = useState<ITypesService[] | undefined>();

  const processingActiveService = (typesService: ITypesService[]) => {
    return typesService?.map(item => ({
      ...item,
      checked: !!organizationTransportTypes.find(status => status.transportType === item.name)?.active,
    }));
  };

  const getTypesService = (selectedService: string) => {
    switch (selectedService) {
      case 'passengers':
      {
        return processingActiveService(typesServicePassengers);
      }
      case 'cargo':
      {
        return processingActiveService(typesServiceCargo);
      }
      case 'carService':
      {
        return typesServiceCarService;
      }
      default:
      {
        return typesServicePassengers;
      }
    }
  };

  useEffect(() => {
    service && setTypesService(getTypesService(service.name));
  }, [service]);

  const numberConnectedServices = typesService?.filter(el => el.checked).length;

  const getChild = () => (
    <>
      <div className={styles.cardDescriptions}>
        <p className={styles.cardTitle}>{service?.title}</p>
        {service.name !== 'carService'
        && <p className={styles.countServicesAdd}>{`Подключено сервисов: ${numberConnectedServices} из ${service?.maxServices}`}</p>}
      </div>
      {imageSrc && (
        <div className={styles.wrapperImage}>
          <img
            className={styles.cardImage}
            src={imageSrc}
            alt={service?.title}
            onLoad={handleLoad}
            style={{ opacity: +isLoaded }}
          />
        </div>
      )}
    </>
  );

  const handleSelectedService = () => {
    setIsModalVisible(true);
    setSelectedService(service.name);
  };

  return (
    <div className={styles.container}>
      {getChild()}
      <div
        className={styles.buttonAddServices}
        onClick={handleSelectedService}
      >
        <img
          src={addIcon}
          alt="Add"
        />
      </div>
    </div>
  );
};
