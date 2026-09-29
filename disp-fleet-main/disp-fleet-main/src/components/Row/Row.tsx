import React, { DetailedHTMLProps, FC, HTMLAttributes } from 'react';
import { Row } from 'antd';
import { RowProps } from 'antd/lib/grid';

import styles from './row.module.scss';

type RowWithContainerProps = RowProps & {
  containerProps?: DetailedHTMLProps<HTMLAttributes<HTMLDivElement>, HTMLDivElement>;
};

/**
 * Антовский Row с оберткой в виде контейнера с overflow: hidden, чтобы row не был шире родителя
 * из-за отступов
 */
export const RowWithContainer: FC<RowWithContainerProps> = ({ containerProps, ...props }) => (
  <div className={styles.container} {...containerProps}>
    <Row gutter={[16, 16]} {...props} />
  </div>
);
