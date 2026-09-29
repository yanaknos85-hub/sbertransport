import React, { FC } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';

import { Button } from 'components/Button';

export const ButtonAdd: FC<{ onClick: () => void }> = ({ onClick }) => {
  const { t } = useTranslation();

  return (
    <Button
      type="primary"
      onClick={onClick}
    >
      <PlusOutlined />
      {' '}
      {t.global.add}
    </Button>
  );
};
