import React, { FC } from 'react';
import { Menu } from 'antd';
import { capitalize, creatingListOfMonths } from 'modules/TaxiRegistry/utils';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { InfoContractor } from 'modules/TaxiRegistry/types/types';

interface OwnProps {
  contractors: Contractor[];
  setVisible(visible: boolean): void;
  menuVisible: boolean;
  setMenuVisible(visible: boolean): void;
  setModalData({
    // @ts-ignore
    contractorId, monthName: monthId, contractorName,
  }: InfoContractor): void;
}

export const DropDownContractorList: FC<OwnProps> = ({
  contractors,
  setVisible,
  menuVisible,
  setMenuVisible,
  setModalData,
}) => {
  const monthsList = creatingListOfMonths();

  const handleClick = (monthName: string, contractorId: string, contractorName: string) => {
    setVisible(true);
    setMenuVisible(false);
    setModalData({
      contractorId,
      monthName,
      contractorName,
    });
  };

  return menuVisible ? (
    <Menu>
      {contractors.map(contractor => (
        <Menu.SubMenu key={`contractor-${contractor.id}`} title={contractor.name}>
          {monthsList.map(month => (
            <Menu.Item
              key={`month-${month.title}`}
              onClick={() => handleClick(month.title, contractor.id, contractor.name)}
            >
              {capitalize(`${month.title} ${month.year}`)}
            </Menu.Item>
          ))}
        </Menu.SubMenu>
      ))}
    </Menu>
  ) : null;
};
