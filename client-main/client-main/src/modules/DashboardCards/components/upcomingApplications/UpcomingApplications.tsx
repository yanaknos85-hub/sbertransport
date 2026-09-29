import { observer } from 'mobx-react';
import * as React from 'react';
import {
  NoActiveApplications,
  NoActiveApplicationsTitle,
  OpenMainContent,
  StyledTitle,
  WrapperOpenMainContent,
  WrapperStyledTitle
} from '../../styled';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import moment from 'moment';
import Carousel from './carousel/Carousel';
import '@ant-design/react-slick';
import arrowUp from 'shared/components/Images/arrowUp.svg';
import arrowDown from 'shared/components/Images/arrowDown.svg';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

const commomParams = {
  pageSetting: {
    page: 0,
    size: 10,
  },
  desiredDate: {
    start: moment().valueOf(),
    end: moment().add(1, 'week').valueOf(),
  },
};

const UpcomingApplications: React.FC = observer(() => {
  const [openWindow, setOpenWindow] = React.useState(true);
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  React.useEffect(() => {
    tripStore.loadUpcomingApplicationsList(commomParams);
  }, []);

  const { isMobile } = usePlatformDetect();

  return (
    <>
      <WrapperStyledTitle>
        <StyledTitle margin="24px 0px 8px 8px">Ближайшие заявки</StyledTitle>
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
          {tripStore.upcomingApplicationsList.length < 1 ? (
            <NoActiveApplications>
              <NoActiveApplicationsTitle>Нет активных заявок</NoActiveApplicationsTitle>
            </NoActiveApplications>
          ) : (
            <Carousel items={tripStore.upcomingApplicationsList} direction={isMobile ? 'column' : 'row'} />
          )}
        </>
      )}
    </>
  );
});

export default UpcomingApplications;
