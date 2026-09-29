import React, { FC } from 'react';
import styles from './rating.module.scss';
import { StarFilled } from '@ant-design/icons';

interface RatingProps {
  rating: number | string;
}

const Rating: FC<RatingProps> = ({ rating }) => (
  <div className={styles.rating}>
    <StarFilled style={{ color: 'rgb(16, 191, 106)' }} />
    {' '}
    {rating.toLocaleString()}
  </div>
);

export default Rating;
