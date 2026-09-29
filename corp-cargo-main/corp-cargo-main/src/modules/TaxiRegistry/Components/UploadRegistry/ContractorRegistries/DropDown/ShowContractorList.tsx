import React, { FC } from 'react';
import { Menu } from 'antd';

import { useContractorMonths } from 'modules/TaxiRegistry/hooks/useContractorMonths';
import { creatingListOfMonths } from 'modules/TaxiRegistry/utils';
import { Contractor } from 'stores/Contractors/Contractors.interface';

import { InfoContractor } from 'modules/TaxiRegistry/types/types';
import { ShowMenuList } from './ShowMenuList';

interface OwnProps {
  contractors: Contractor[];
  setVisible(visible: boolean): void;
  isContractorsLoading: boolean;
  setMenuVisible(visible: boolean): void;
  menuVisible: boolean;
  setModalData({
    contractorId, monthName, contractorName,
  }: InfoContractor): void;
}

export const ShowContractorList: FC<OwnProps> = ({
  contractors,
  setVisible,
  isContractorsLoading,
  setMenuVisible,
  menuVisible,
  setModalData,
}) => {
  const monthList = creatingListOfMonths();
  const { isLoading, monthListContractor } = useContractorMonths();
  return menuVisible ? (
    !isLoading && !isContractorsLoading && contractors.length ? (
      <Menu>
        {contractors.map(contractor => (
          <Menu.SubMenu key={contractor.id} title={contractor.name}>
            {monthList.map((month, monthInd) => (
              <ShowMenuList
                key={`${contractor.id}-${month.id}`}
                setVisible={setVisible}
                setMenuVisible={setMenuVisible}
                setModalData={setModalData}
                month={month}
                monthListContractor={monthListContractor}
                contractorId={contractor.id}
                contractorName={contractor.name}
                monthInd={monthInd}
              />
            ))}
          </Menu.SubMenu>
        ))}
      </Menu>
    ) : null
  ) : null;
};
