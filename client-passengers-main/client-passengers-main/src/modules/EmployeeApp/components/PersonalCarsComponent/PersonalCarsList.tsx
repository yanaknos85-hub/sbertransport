import { PlusOutlined } from '@ant-design/icons';
import { Button, PageHeader, Table } from 'antd';
import React from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { useDeletePersonalCar } from 'api/personalCars';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { PageContent } from 'shared/components/PageContent/PageContent';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { usePersonalCars } from '../CreateTripRequest/hooks/usePersonalCars';
import { columns } from './Components/Columns';
import { PersonalCarsTexts, PersonalCarsTextsCyrillic } from './PersonalCars.constants';

import './override.scss';

export const PersonalCarsList: React.FC = (): JSX.Element => {
  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();
  const { selfEmployee } = employeeStore;

  const { personalCars, refetch } = usePersonalCars();
  const [deletePersonalCar] = useDeletePersonalCar(selfEmployee);

  const deleteCar = (autoId: string) => {
    deletePersonalCar({ autoId }).then(() => {
      refetch();
    });
  };

  const match = useRouteMatch();
  const history = useHistory();

  const footerText = PersonalCarsTextsCyrillic[PersonalCarsTexts.addNew];

  const handleClick = (): void => {
    history.push(`${match.path}/adding`);
  };
  const renderFooter = (): JSX.Element => (
    <div className="table_footer">
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={handleClick}
      >
        {footerText}
      </Button>
    </div>
  );

  return (
    <div className="personal-cars-list-table">
      <PageHeader title={EmployeeAppLinksTitles[EmployeeAppLinks.personalCars]} />
      <PageContent>
        <Table
          bordered={true}
          dataSource={personalCars}
          columns={columns(match.path, deleteCar)}
          rowClassName="editable-row"
          rowKey="id"
          size="small"
          tableLayout="fixed"
          footer={() => renderFooter()}
        />
      </PageContent>
    </div>
  );
};
