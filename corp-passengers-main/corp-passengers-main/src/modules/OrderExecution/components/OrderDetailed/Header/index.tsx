import React, { FC, SyntheticEvent, ReactChild } from 'react';
import { Link, useHistory } from 'react-router-dom';
import { Divider } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import cn from 'classnames';

import * as routes from 'constants/constants.routes';

import styles from './styles.module.scss';

interface OrderDetailedHeaderProps {
  caption: string;
  children?: ReactChild;
  className?: {
    title?: string;
    container?: string;
  };
}

const OrderDetailedHeader: FC<OrderDetailedHeaderProps> = props => {
  const {
    caption, children, className,
  } = props;
  const history = useHistory();

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
  };

  return (
    <div>
      <div className={cn(styles.orderDetailedHeader, className?.container)}>
        <Link
          className={cn(styles.orderDetailedHeader__title, className?.title)}
          to={routes.ORDER_EXECUTION_PASSENGERS}
          onClick={goBack}
        >
          <ArrowLeftOutlined />
          {caption}
        </Link>
        {children}
      </div>
      <Divider />
    </div>
  );
};

export default OrderDetailedHeader;
