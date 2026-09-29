import React from 'react';
import { TripRequestModel } from 'stores/Trip/models';
import CompensationImageWrapper from './CompensationImageWrapper';
import styles from './styles.module.scss';
import { UUID } from 'utils/io-ts';

const CompensationInner = ({ request }: { request: TripRequestModel }): JSX.Element | null => {
  const { transportCompensation } = request;

  return (
    <div className={styles.compensationInner}>
      {(transportCompensation || []).map(x => (
        <CompensationImageWrapper key={x.attachedDocumentId} document={{ id: x.attachedDocumentId as UUID }} />
      ))}
    </div>
  );
};

export default CompensationInner;
