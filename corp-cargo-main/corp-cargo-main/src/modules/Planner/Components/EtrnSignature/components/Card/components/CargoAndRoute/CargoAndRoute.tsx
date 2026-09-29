import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import cargoIcon from '../../icons/cargo.png';
import routeIcon from '../../icons/route.png';

import styles from './styles.module.scss';

interface ParticipantBlockProps {
  cargo: string;
  route: string;
}

interface Props {
  data: ParticipantBlockProps;
}

export const CargoAndRoute: FC<Props> = props => {
  const { cargo, route } = props.data;
  const { t: { Etrn } } = useTranslation();

  return (
    <div className={styles.block}>
      <div className={styles.title}>{Etrn.card.cargo}</div>
      <div className={styles.row}>
        <div className={styles.field}>
          <img
            src={cargoIcon}
            alt="cargo"
            width={24}
            height={24}
          />
          <span className={styles.label}>{Etrn.card.cargoLabel}</span>
          <span className={styles.value}>{cargo}</span>
        </div>
        <div className={styles.field}>
          <img
            src={routeIcon}
            alt="route"
            width={24}
            height={24}
          />
          <span className={styles.label}>{Etrn.card.routeLabel}</span>
          <span className={styles.value}>{route}</span>
        </div>
      </div>
    </div>
  );
};

export default CargoAndRoute;
