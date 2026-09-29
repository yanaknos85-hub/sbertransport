import { observer } from 'mobx-react';
import React, { useState, FC } from 'react';
import {
  OpenMainContent,
  StyledWidgetsTitle,
  WidgetCard,
  WrapperOpenWidgetsMainContent,
  WrapperStyledWidgetsTitle,
  WrapperTitle
} from '../../styled';
import '@ant-design/react-slick';
import { ServiceWidgetsEnum } from 'modules/DashboardCards/constants/widgets.constants';
import arrowUp from 'shared/components/Images/arrowUp.svg';
import arrowDown from 'shared/components/Images/arrowDown.svg';
import ChoosingService from './choosingService/ChoosingService';
import QuickOrderWidget from './quickOrderWidget/QuickOrderWidget';
import PaymentsWidget from './paymentsWidget/PaymentsWidget';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

const Widgets: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(true);
  const [selected, setSelected] = useState(ServiceWidgetsEnum.ALL);

  const { isMobile } = usePlatformDetect();

  return (
    <>
      <WrapperStyledWidgetsTitle>
        <WrapperTitle>
          <StyledWidgetsTitle>Виджеты</StyledWidgetsTitle>
          <ChoosingService selected={selected} chandgeSelected={setSelected} />
        </WrapperTitle>
        <WrapperOpenWidgetsMainContent>
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
        </WrapperOpenWidgetsMainContent>
      </WrapperStyledWidgetsTitle>
      {openWindow && (
        <>
          <WidgetCard direction={isMobile ? 'column' : 'row'}>
            {(selected === ServiceWidgetsEnum.ALL || selected === ServiceWidgetsEnum.PASSENGER) && (
              <QuickOrderWidget fullWidth={isMobile} />
            )}
            <PaymentsWidget fullWidth={isMobile} />
          </WidgetCard>
        </>
      )}
    </>
  );
});

export default Widgets;
