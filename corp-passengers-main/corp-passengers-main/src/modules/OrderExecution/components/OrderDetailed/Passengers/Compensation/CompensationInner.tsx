import React from 'react';
import { UUID } from 'utils/io-ts';
import { TripRequestModel } from 'stores/Trip/models/TripRequest.model';
import CompensationImageWrapper from './CompensationImageWrapper';
import styles from './styles.module.scss';

const CompensationInner = ({ request }: { request: TripRequestModel }): JSX.Element | null => {
  const { transportCompensation } = request;

  return (
    <div className={styles.compensationInner}>
      {(transportCompensation || []).map(x => x.attachedDocumentId && (
        <CompensationImageWrapper
          key={x.id}
          document={{
            id: x.attachedDocumentId as UUID,
            fileFormat: x.compensationDocumentDTO?.fileFormat,
            fileName: x.compensationDocumentDTO?.fileName,
          }}
        />
      ))}
    </div>
  );
};

export default CompensationInner;
