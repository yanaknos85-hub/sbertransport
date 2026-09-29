import React, { useState } from 'react';
import type { FC } from 'react';
import {
  OpenMainContent,
  StyledTitle,
  WrapperOpenMainContent,
  WrapperStyledTitle,
  WrapperUsefulMaterials,
  WrapperUsefulMaterialsMobile
} from 'modules/DashboardCards/styled';
import '@ant-design/react-slick';
import arrowUp from 'shared/components/Images/arrowUp.svg';
import arrowDown from 'shared/components/Images/arrowDown.svg';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { ServiceType } from 'modules/DashboardCards/constants/general.constants';
import { ServiceTypeTitles } from 'modules/DashboardCards/constants/usefulMaterials.constants';
import UsefulMaterialsItems from './UsefulMaterialsItem/UsefulMaterialsItem';
import Instructions from './instructions.json';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { WrapperUsefulMaterialsItems, WrapperUsefulMaterialsItemsMobile } from './UsefulMaterialsItem/styled';

const tabs: TabsNavOption[] = Object.keys(ServiceTypeTitles).map(key => ({
  key,
  label: ServiceTypeTitles[key],
}));

const UsefulMaterials: FC = () => {
  const [openWindow, setOpenWindow] = useState(true);
  const [filter, setFilter] = useState(ServiceType.ALL);
  const { isMobile, isDesktop } = usePlatformDetect();

  return (
    <>
      <WrapperStyledTitle>
        <StyledTitle margin="24px 0px 8px 8px">Инструкции</StyledTitle>
        <WrapperOpenMainContent>
          {openWindow ? (
            <>
              <OpenMainContent onClick={() => setOpenWindow(prev => !prev)}>Cкрыть</OpenMainContent>
              <img src={arrowUp} alt="arrowDown" />
            </>
          ) : (
            <>
              <OpenMainContent onClick={() => setOpenWindow(prev => !prev)}>Раскрыть</OpenMainContent>
              <img src={arrowDown} alt="arrowUp" />
            </>
          )}
        </WrapperOpenMainContent>
      </WrapperStyledTitle>
      {openWindow && (
        <>
          {isDesktop && (
            <WrapperUsefulMaterials>
              <TabsNav
                currentKey={filter}
                options={tabs}
                defaultActiveTab={tabs[0]}
                onChange={setFilter}
              />
              <WrapperUsefulMaterialsItems>
                <UsefulMaterialsItems filter={filter} allInstructions={Instructions} />
              </WrapperUsefulMaterialsItems>
            </WrapperUsefulMaterials>
          )}
          {isMobile && (
            <WrapperUsefulMaterialsMobile>
              <WrapperUsefulMaterialsItemsMobile>
                <UsefulMaterialsItems filter={ServiceType.ALL} allInstructions={Instructions} />
              </WrapperUsefulMaterialsItemsMobile>
            </WrapperUsefulMaterialsMobile>
          )}
        </>
      )}
    </>
  );
};

export default UsefulMaterials;
