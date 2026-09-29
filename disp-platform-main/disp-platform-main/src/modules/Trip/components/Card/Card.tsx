import React, { FC, HTMLProps } from 'react';
import styles from './badge.module.scss';

const Card: FC<HTMLProps<HTMLDivElement>> = props => <div {...props} className={styles.card} />;

export default Card;
