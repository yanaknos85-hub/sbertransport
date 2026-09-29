import React, { useState } from 'react';
import type { FC } from 'react';
import {
  OpenMainContent,
  StyledTitle,
  WrapperOpenMainContent,
  WrapperStyledTitle
} from 'modules/DashboardCards/styled';
import { WrapperContent } from './styled';
import '@ant-design/react-slick';
import arrowUp from 'shared/components/Images/arrowUp.svg';
import arrowDown from 'shared/components/Images/arrowDown.svg';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { ServiceType } from 'modules/DashboardCards/constants/general.constants';
import { ServiceTypeTitles } from 'modules/DashboardCards/constants/faq.constants';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import List from './List';
import { itemsByService } from './items';

const tabs: TabsNavOption[] = Object.keys(ServiceTypeTitles).map(key => ({
  key,
  label: ServiceTypeTitles[key],
}));

const FAQ: FC = () => {
  const [openWindow, setOpenWindow] = useState(true);
  const [filter, setFilter] = useState(ServiceType.ALL);
  const { isMobile } = usePlatformDetect();
  const items = itemsByService[filter];

  if (isMobile) {
    return null;
  }

  return (
    <>
      <WrapperStyledTitle>
        <StyledTitle margin="24px 0px 8px 8px">Часто задаваемые вопросы</StyledTitle>
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
          <WrapperContent>
            <TabsNav
              currentKey={filter}
              options={tabs}
              defaultActiveTab={tabs[0]}
              onChange={setFilter}
            />
            {!!items?.length && (
              <List items={items} />
            )}
          </WrapperContent>
        </>
      )}
    </>
  );
};

export default FAQ;
