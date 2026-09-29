import React, { FC, SyntheticEvent, ReactChild } from 'react';
import { Link } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { ArrowLeftOutlined } from '@ant-design/icons';

import * as routes from 'constants/constants.routes';

import styles from './styles.module.scss';

interface OrderDetailedHeaderProps {
  caption: string;
  children?: ReactChild;
}

const OrderDetailedHeader: FC<OrderDetailedHeaderProps> = props => {
  const { caption, children } = props;
  const history = useHistory();

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
  };

  return (
    <div className={styles.orderDetailedHeader}>
      <Link
        className={styles.orderDetailedHeader__title}
        to={routes.MULTI_LOGISTICS}
        onClick={goBack}
      >
        <ArrowLeftOutlined />
        {caption}
      </Link>
      {children}
    </div>
  );
};

export default OrderDetailedHeader;
