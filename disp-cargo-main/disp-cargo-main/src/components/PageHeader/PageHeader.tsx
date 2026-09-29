import { PageHeader as AntdPageHeader } from 'antd';
import { PageHeaderProps } from 'antd/lib/page-header';
import React, { FC } from 'react';
import styles from './pageHeader.module.scss';

const PageHeader: FC<PageHeaderProps> = props => <AntdPageHeader className={styles.pageHeader} {...props} />;

export default PageHeader;
