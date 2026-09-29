import React from 'react';
import { HTMLProps } from 'react';
import styles from './textButton.module.scss';

export default (props: HTMLProps<HTMLDivElement>) => <div className={styles.textButton} {...props} />;
