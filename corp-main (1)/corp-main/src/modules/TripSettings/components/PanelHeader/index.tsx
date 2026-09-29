import React, { FC } from 'react';

import styles from './styles.module.scss';

const PanelHeader: FC<{ title: string; subTitle: string }> = ({ title, subTitle }) => (
  <>
    {title && <h2 className={styles.title}>{title}</h2>}
    {subTitle && <p className={styles.subtitle}>{subTitle}</p>}
    <hr className={styles.headerDivider} />
  </>
);

export default PanelHeader;
