import React, { FC } from 'react';

import styles from './row.module.scss';

const Row: FC = ({ children }) => <div className={styles.row}>{children}</div>;

export default Row;
