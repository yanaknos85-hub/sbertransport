import React, { FC } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';
import styles from './ExportButton.module.scss';

export const ExportButton: FC<{ onClick: () => void }> = ({ onClick }) => {
  const { t } = useTranslation();

  return (
    <Button
      className={styles.exportButton}
      type="default"
      onClick={onClick}
    >
      <PlusOutlined />
      {' '}
      {t.global.addXls}
    </Button>
  );
};
