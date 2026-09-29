import styled from 'styled-components';

const ProfileWrapper = styled.div`
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background: rgb(255, 255, 255);
  width: 100%;

  .ant-divider {
    width: 100% !important;
    margin: 16px 0 !important;

    @media (min-width: 501px) {
      margin: 24px 0 !important;
    }
  }
`;

const HeaderProfileNavigate = styled.div`
  margin: 5px 0 11px 0;
  display: flex;
  align-items: center;

  @media (max-width: 500px) {
    padding: 0 8px;
  }

  svg {
    margin: 0 16px;
  }

  a {
    color: rgb(38, 38, 38);
    font-family: SB Sans Text;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: -0.3px;
    cursor: pointer;
  }
`;

const TitleNavigate = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
  cursor: pointer;
`;

export { ProfileWrapper, HeaderProfileNavigate, TitleNavigate };
