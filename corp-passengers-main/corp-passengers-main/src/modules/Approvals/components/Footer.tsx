import { PlusOutlined } from '@ant-design/icons';
import React, { FC } from 'react';
import Button from 'antd/lib/button';
import { useTranslation } from 'i18n';

interface FooterProps {
  onClick?: () => void;
}

const Footer: FC<FooterProps> = ({ onClick }) => {
  const { t } = useTranslation();

  return (
    <div className="table_footer">
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={() => onClick && onClick()}
        className="add-contract-button"
      >
        {t.global.add}
      </Button>
    </div>
  );
};

export { Footer };
