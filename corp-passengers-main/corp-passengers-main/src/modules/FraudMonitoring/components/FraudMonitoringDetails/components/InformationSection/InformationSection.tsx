import React from 'react';

import { VALUE_NOT_FOUND } from 'constants/constants.app';

import { TFraudMonitoringDetailsResponse } from 'modules/FraudMonitoring/fraudMonitoring.interface';

import { InformationBlock } from './components/InformationBlock/InformationBlock';

import { useInformationGroups } from './hooks/useInformationGroups';

import styles from './styles.module.scss';

export const InformationSection = ({ fraudDetails }: { fraudDetails: TFraudMonitoringDetailsResponse }) => {
  const groups = useInformationGroups(fraudDetails);

  return (
    <div className={styles.container}>
      {groups.map(
        ({
          groupName, groupNameDescription, fields,
        }, index) => fields.length !== 0 && (
        <InformationBlock key={index}>
          <InformationBlock.Header>
            <InformationBlock.Title>{groupName}</InformationBlock.Title>
            {groupNameDescription && (
            <InformationBlock.Description>{groupNameDescription}</InformationBlock.Description>
            )}
          </InformationBlock.Header>
          <InformationBlock.Body>
            {fields.map(({ title, value }, index) => (
              <InformationBlock.Field title={title} key={index}>
                {value !== null && value !== undefined ? value : VALUE_NOT_FOUND}
              </InformationBlock.Field>
            ))}
          </InformationBlock.Body>
        </InformationBlock>
        )
      )}
    </div>
  );
};
