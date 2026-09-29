import React from 'react';
import classNames from 'classnames';
import { useTranslation } from 'i18n';

import { ReactComponent as ExclamationIcon } from 'shared/icons/exclamation-icon.svg';
import { ReactComponent as ChevronUp } from 'shared/icons/chevron-up.svg';

import { useModalState } from 'shared/hooks/useModal';

import { TFraudMonitoringDetailsResponse } from 'modules/FraudMonitoring/fraudMonitoring.interface';

import styles from './styles.module.scss';

export const FraudSection = ({ fraudItems }: { fraudItems: TFraudMonitoringDetailsResponse['fraudMarkers'] }) => {
  const fraudNames = fraudItems.map(({ comment }) => comment);
  const {
    t: {
      fraudMonitoring: { details },
    },
  } = useTranslation();
  const [isOpened, { toggle: toggleAdditionalInfo }] = useModalState(true);

  const handleTitleClicked = () => toggleAdditionalInfo();

  return (
    <div className={styles.container}>
      <div
        className={styles.title}
        onClick={handleTitleClicked}
        data-testid="fraud-section-header"
      >
        <ExclamationIcon />
        <h3 className={styles.text}>{details.fraudSectionTitle}</h3>
        <ChevronUp className={classNames(styles.openMarker, { [styles.closed]: !isOpened })} />
      </div>
      {isOpened && (
        <div className={styles.body} data-testid="fraud-section-body">
          <p className={styles.text}>{details.fraudSectionText}</p>
          <div className={styles.fraudContainer}>
            <ul className={styles.fraudList}>
              {fraudNames.map((name, index) => (
                <li
                  className={styles.fraudListItem}
                  key={index}
                  data-testid="fraud-list-item"
                >
                  {name}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
};
