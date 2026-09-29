import React, { FC } from 'react';
import { useParams } from 'react-router-dom';
import CargoComment from 'shared/components/Cargo/CargoComment';
import CargoEngComment from 'shared/components/Cargo/CargoEngComment';

import { useGetDetailed } from 'modules/Exchange/api/exchange';

import { CargoList } from './CargoList/CargoList';
import { Header } from './Header/Header';
import { Map } from './Map/Map';

import styles from './Detailed.module.scss';

export const ExchangeDetailed: FC = () => {
  const { id } = useParams<{ type: string; id: string }>();

  const { data: request } = useGetDetailed(id);

  return (
    <div className={styles.cargoLayoutWrapper}>
      <div className={styles.cargoLayout}>
        <div className={styles.cargoContainer}>
          <div className={styles.cargoMainContentFull}>
            <Header request={request} />
            <Map request={request} />
            <CargoList cargoDetails={request.cargoDetails} />
            <CargoComment comment={request.comment} />
            <CargoEngComment comment={request?.commentEng} />
          </div>
        </div>
      </div>
    </div>
  );
};
