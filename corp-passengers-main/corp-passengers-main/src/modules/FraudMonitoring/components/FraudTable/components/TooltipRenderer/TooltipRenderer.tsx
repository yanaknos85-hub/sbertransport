import React from 'react';
import { Tooltip } from 'antd';

import { useIsOverflowing } from '../../hooks/useIsOverflowing';

import { ToolipRendererProps } from './types';

import styles from './styles.module.scss';

export const ToolipRenderer = ({ children, textAlign = 'left' }: ToolipRendererProps) => {
  const { isOverflowing, ref } = useIsOverflowing<HTMLParagraphElement>();

  return isOverflowing ? (
    <Tooltip
      placement="bottomLeft"
      title={children}
      overlayClassName={styles.tooltip}
    >
      <p
        className={styles.text}
        style={{ textAlign }}
        ref={ref}
      >
        {children}
      </p>
    </Tooltip>
  ) : (
    <p
      className={styles.text}
      style={{ textAlign }}
      ref={ref}
    >
      {children}
    </p>
  );
};
