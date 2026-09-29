import styled from 'styled-components';

const StyledHeaderProfile = styled.div`
  display: flex;
  padding: 16px 16px 0 16px;
  width: 100%;

  @media (min-width: 501px) {
    padding: 24px 24px 0 24px;
  }
`;

const BasicInformationWrapper = styled.div`
  display: flex;

  @media (max-width: 500px) {
    .ant-form {
      height: 100px;
    }
  }
`;

const BasicInformation = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-left: 24px;
  gap: 8px;

  @media (min-width: 501px) {
    padding: 7px 0 7px 33px;
    justify-content: space-between;
  }
`;

const UserFullNameWrapper = styled.div`
  display: flex;
  flex-direction: column;
`;

const UserName = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 16px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.45px;

  @media (min-width: 501px) {
    font-size: 24px;
    line-height: 30px;
  }
`;

const PostInformation = styled.div`
  display: flex;
  flex-direction: column;
`;

const Position = styled.span`
  color: rgb(144, 144, 144);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 18px;
  letter-spacing: -0.15px;
  margin-bottom: 5px;

  @media (min-width: 501px) {
    margin-bottom: 12px;
  }
`;

const ServiceNumber = styled.span`
  color: rgb(144, 144, 144);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 18px;
  letter-spacing: -0.15px;
`;

const UserAvatar = styled.img`
  height: 100px;
  width: 100px;
  border-radius: 50%;
  object-fit: cover;

  @media (min-width: 501px) {
    height: 140px;
    width: 140px;
  }
`;

const CourierWrapper = styled.div`
  display: flex;
  align-items: center;
  margin-left: auto;
  cursor: pointer;

  @media (max-width: 500px) {
    display: none;
  }
`;

const SwitchWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const CourierButtonWrapper = styled.div`
  cursor: pointer;
`;

const PaddingText = styled.div`
  padding-bottom: 24px;
`;

export {
  StyledHeaderProfile,
  BasicInformationWrapper,
  BasicInformation,
  UserFullNameWrapper,
  PostInformation,
  ServiceNumber,
  Position,
  UserName,
  UserAvatar,
  CourierButtonWrapper,
  SwitchWrapper,
  CourierWrapper,
  PaddingText
};
