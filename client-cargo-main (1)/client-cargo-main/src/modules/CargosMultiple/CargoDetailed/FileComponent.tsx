import React from 'react';
import TTypography from 'shared/components/Cargo/TTypography';
import styled from 'styled-components';

import { DRIVER_INFO_STATUSES } from 'constants/CargoRequestStatuses.constants';

import { ReactComponent as PdfIcon } from '../static/images/pdfIcon.svg';
import { ReactComponent as TriplePoint } from '../static/images/triplePoint.svg';

import styles from './styles.module.scss';

const File = styled.div<{ status?: string }>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: ${props => (props.status && DRIVER_INFO_STATUSES.has(props.status) ? '24px 20px' : '15px 20px')};
  background-color: var(--dark-50);
  border-radius: 12px;
`;

interface Props {
  imageUrl?: string;
  status?: string;
}

export const FileComponent: React.FC<Props> = props => {
  const { imageUrl, status } = props;
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement, MouseEvent>) => {
    e.preventDefault();
  };

  return (
    <a
      href={imageUrl}
      target="_blank"
      rel="noreferrer"
      onClick={handleClick}
      className={styles.externalLink}
    >
      <File status={status}>
        <div className={styles.fileContainer}>
          <PdfIcon />
        </div>
        <div className={styles.textContainer}>
          <TTypography>Накладная.pdf</TTypography>
        </div>
        <div>
          <TriplePoint />
        </div>
      </File>
    </a>
  );
};
