import React, { FC } from 'react';

import styles from './ContainerWidget.module.scss';

export const ContainerWidget: FC = ({ children }) => <div className={styles.widgetContainer}>{children}</div>;
