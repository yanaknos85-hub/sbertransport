import React, { FC, ReactNode } from 'react';
import Panel from 'components/Panel/Panel';
import styles from './Card.module.scss';

interface Props {
  title: ReactNode;
}

const Card: FC<Props> = ({ title, children }) => {
  return (
    <Panel>
      <div className={styles.container}>
        <span className={styles.container__header}>{title}</span>

        <div className={styles.container__content}>
          {children}
        </div>
      </div>
    </Panel>
  );
};

export default Card;
