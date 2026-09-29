import styled from 'styled-components';

export const WrapperStory = styled.div`
  .ant-progress-bg {
    background-color: #ffffff;
    transition: width 0.5s ease;
    width: 100%;
    height: 2.5px !important;
    border-radius: 10px;
  }

  .ant-progress {
    width: 100px;
  }

  .ant-progress-inner {
    transition: width 0.5s ease;
    background-color: rgba(255, 255, 255, 0.32);
    width: 81px;
    height: 2.5px;
  }
`;

export const StoryHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 42px 0 64px;
  position: relative;
`;

export const ProgressContainer = styled.div`
  width: 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
`;

export const ProgressItem = styled.div`
  width: 81px;
  display: flex;
  align-items: center;
  margin-right: 5px;
  cursor: pointer;
`;

export const CloseButton = styled.img`
  position: absolute;
  right: 44px;
  top: 0;
  cursor: pointer;
`;

export const WrapperMainImage = styled.div`
  display: flex;
  justify-content: center;
`;

export const WrapperInfoStory = styled.div`
  width: 70%;
`;

export const MainImage = styled.img`
  width: 100%;
  cursor: pointer;
`;

export const WrapperDescription = styled.div`
  display: flex;
  flex-direction: column;
  margin: 25px 0 0 51px;
`;

export const DescriptionTitle = styled.span`
  color: #ffffff;
  font-family: SB Sans Interface;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  word-break: break-all;
`;

export const DescriptionText = styled.span`
  color: #ffffff;
  font-family: SB Sans Text;
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  margin-top: 8px;
  white-space: pre-line;
`;
