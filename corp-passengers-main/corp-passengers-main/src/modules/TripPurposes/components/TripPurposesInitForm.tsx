import React from 'react';

import { Button } from 'shared/components/Button/Button';
import EmptyFolder from 'shared/images/EmptyFolder.png';

import { useTranslation } from 'i18n';

import styles from './TripPurposes.module.scss';

export const TripPurposesInitForm: React.FC<{ onCreate: () => void }> = ({ onCreate }) => {
  const {
    t: {
      SettingsTripRules: { tripPurposes },
    },
  } = useTranslation();

  return (
    <div className={styles.initFormWrapper}>
      <div className={styles.folderImage}>
        <img src={EmptyFolder} alt="empty folder" />
      </div>

      <div className={styles.initFormTitle}>{tripPurposes.initFormTitle}</div>

      <div className={styles.initFormDescrtiption}>
        {tripPurposes.initFormDescrtiption}
        {tripPurposes.initFormTemplateLinkText}
      </div>

      <div className={styles.greenLink}>{tripPurposes.initFormHowToLinkText}</div>

      <div className={styles.buttonsContainer}>
        <Button size="large" htmlType="button">
          {tripPurposes.initFormUploadButtonCaption}
        </Button>
        <Button
          size="large"
          htmlType="button"
          type="primary"
          onClick={onCreate}
        >
          {tripPurposes.initFormSetupButtonCaption}
        </Button>
      </div>
    </div>
  );
};
