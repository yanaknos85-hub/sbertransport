import React from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { DepartmentsTexts, DepartmentsTextsCyrillic } from '../../Departments.constants';
import styles from './footer.module.scss';

const Footer = (): JSX.Element => {
  const history = useHistory();
  const match = useRouteMatch();
  const handleClick = () => history.push(`${match.path}/adding`);

  return (
    <div className={styles.footer}>
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={handleClick}
      >
        {DepartmentsTextsCyrillic[DepartmentsTexts.addNew]}
      </Button>
    </div>
  );
};

export { Footer };
