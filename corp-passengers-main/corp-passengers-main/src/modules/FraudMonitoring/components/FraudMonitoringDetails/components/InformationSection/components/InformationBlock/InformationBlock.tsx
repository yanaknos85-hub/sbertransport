import React from 'react';

import {
  InformationBlockBodyProps,
  InformationBlockDescriptionProps,
  InformationBlockFieldProps,
  InformationBlockHeaderProps,
  InformationBlockProps,
  InformationBlockTitleProps
} from './types';

import styles from './styles.module.scss';

export function InformationBlock({ children }: InformationBlockProps) {
  return <div className={styles.infomationBlock} data-testid="information-block">{children}</div>;
}

InformationBlock.Header = ({ children }: InformationBlockHeaderProps) => {
  return <div className={styles.header}>{children}</div>;
};

InformationBlock.Title = ({ children }: InformationBlockTitleProps) => {
  return <h3 className={styles.title}>{children}</h3>;
};

InformationBlock.Description = ({ children }: InformationBlockDescriptionProps) => {
  return <p className={styles.description}>{children}</p>;
};

InformationBlock.Body = ({ children }: InformationBlockBodyProps) => {
  return <div className={styles.body}>{children}</div>;
};

InformationBlock.Field = ({ children, title }: InformationBlockFieldProps) => {
  return (
    <div className={styles.field} data-testid="information-block-field">
      <p className={styles.field_title}>{title}</p>
      <p className={styles.field_value} data-testid="information-block-field-value">{children}</p>
    </div>
  );
};
