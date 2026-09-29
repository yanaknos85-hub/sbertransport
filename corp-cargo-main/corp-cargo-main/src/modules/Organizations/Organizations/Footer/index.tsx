import React from 'react';
import { useTranslation } from 'i18n';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

import style from './footer.module.scss';

const Footer: React.FC = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const match = useRouteMatch();

  const handleAddNewClick = (): void => {
    history.push(`${match.path}/adding`);
  };

  return (
    <div className={`table_footer ${style.footer}`}>
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={handleAddNewClick}
      >
        {t.global.addOrganization}
      </Button>
    </div>
  );
};

export default Footer;
