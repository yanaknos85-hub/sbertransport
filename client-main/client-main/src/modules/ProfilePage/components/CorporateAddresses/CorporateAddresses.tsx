import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import {
  StyledMyAddresses, WrapperStyledTitle, StyledTitle, WrapperOpenMainContent, OpenMainContent, WrapperListsInfoAddresses, WrapperListItem, WrapperInfoAdresses, TitleMyAddresses, InfoAddresses, List, WrapperMainInfo, WrapperButtonOpenMainContent
} from './styled';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import { ReactComponent as CorporateAddressesIcon } from 'shared/components/Images/CorporateAddressesIcon.svg';

const CorporateAddresses: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(false);
  const { addressStore } = useAppStoreContext();
  const { selfCorporateList } = addressStore;

  return (
    <>
      <StyledMyAddresses>
        <WrapperStyledTitle openWindow={openWindow}>
          <StyledTitle>Корпоративные адреса</StyledTitle>
          <WrapperOpenMainContent>
            {openWindow
              ? (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Cкрыть
                  </OpenMainContent>
                  <ArrowUp />
                </WrapperButtonOpenMainContent>
              )
              : (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Раскрыть
                  </OpenMainContent>
                  <ArrowDown />
                </WrapperButtonOpenMainContent>
              )}
          </WrapperOpenMainContent>
        </WrapperStyledTitle>
      </StyledMyAddresses>
      {openWindow
      && (
      <WrapperListsInfoAddresses>
        <List>
          {selfCorporateList.map(el => (
            <WrapperListItem key={el.id}>
              <CorporateAddressesIcon />
              <WrapperMainInfo>
                <WrapperInfoAdresses>
                  <TitleMyAddresses>
                    {el?.label}
                  </TitleMyAddresses>
                  <InfoAddresses>
                    {`${el?.address.street && `${el?.address.street}, `}${el?.address.house && `${el?.address.house}, `}${el?.address.region && `${el?.address.region},`}`}
                  </InfoAddresses>
                </WrapperInfoAdresses>
              </WrapperMainInfo>
            </WrapperListItem>
          )
          )}
        </List>
      </WrapperListsInfoAddresses>
      )}
    </>
  );
});

export default CorporateAddresses;
