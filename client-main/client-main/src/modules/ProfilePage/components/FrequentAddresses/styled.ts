import styled from 'styled-components';

const StyledMyAddresses = styled.div`
  display: flex;
  padding: 0px 16px;
  width: 100%;

  @media (min-width: 501px) {
    padding: 0 24px;
  }
`;

const WrapperStyledTitle = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
  width: 100%;
  align-items: center;
`;

const StyledTitle = styled.div`
  color: rgb(0, 0, 0);
  font-family: SB Sans Interface;
  font-size: 18px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: 0%;
`;

const WrapperOpenMainContent = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
`;

const OpenMainContent = styled.span`
  margin-right: 8px;
  color: rgb(16, 191, 106);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
  cursor: pointer;
`;

const WrapperListsInfoAddresses = styled.div`
  padding: 0 16px;

  @media (min-width: 501px) {
    padding: 0 24px;
  }
`;

const WrapperListItem = styled.div`
  width: 100%;
  background: rgb(255, 255, 255);
  border: 1px solid rgb(236, 236, 236);
  border-radius: 12px;
  display: flex;
  min-width: 340px;
  padding: 15px;

  @media (max-width: 500px) {
    &:not(:last-child) {
      margin: 0 0 9px;
    }
  }

  @media (min-width: 501px) {
    width: 31.6%;
    margin: 0 9px 9px 9px;
  }

  svg {
    margin-top: 3px;
  }
`;

const WrapperInfoAdresses = styled.div`
  display: flex;
  flex-direction: column;
  margin-left: 16px;
`;

const TitleMyAddresses = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

const InfoAddresses = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

const List = styled.div`
  display: flex;
  flex-wrap: wrap;
  margin: 0 -9px;
`;

const WrapperMainInfo = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-between;

  & > span {
    svg {
      fill: #909090;
    }

    svg:hover {
      cursor: pointer;
      fill: red;
    }
  }
`;

const WrapperButtonOpenMainContent = styled.div`
  display: flex;
  align-items: center;
`;

export {
  WrapperButtonOpenMainContent,
  WrapperMainInfo,
  List,
  InfoAddresses,
  TitleMyAddresses,
  WrapperInfoAdresses,
  WrapperListItem,
  WrapperListsInfoAddresses,
  StyledMyAddresses,
  WrapperStyledTitle,
  StyledTitle,
  WrapperOpenMainContent,
  OpenMainContent
};
