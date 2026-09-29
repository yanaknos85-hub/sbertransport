import React, { FC } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';
import styles from './ButtonAdd.module.scss';

export const ButtonAdd: FC<{ onClick: () => void }> = ({ onClick }) => {
  const { t } = useTranslation();

  return (
    <Button
      className={styles.buttonAdd}
      type="primary"
      onClick={onClick}
    >
      <PlusOutlined />
      {' '}
      {t.global.add}
    </Button>
  );
};
