import React from 'react';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useHistory } from '@sber-sbertransport/mf-core';
import { EmployeesHandbookTexts, EmployeesHandbookTextsCyrillic } from '../../Employees.constants';
import * as routes from 'constants/constants.routes';
import styles from './footer.module.scss';

export const Footer: React.FC = () => {
  const history = useHistory();
  const createEmployee = () => history.push(routes.EMPLOYEE_ADD);

  return (
    <div className={styles.footer}>
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={createEmployee}
      >
        {EmployeesHandbookTextsCyrillic[EmployeesHandbookTexts.addNew]}
      </Button>
    </div>
  );
};
