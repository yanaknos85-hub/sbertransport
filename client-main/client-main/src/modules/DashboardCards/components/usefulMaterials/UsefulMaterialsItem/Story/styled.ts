/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const WrapperStory = styled.div`
  backgroundColor: #ffffff;

  .ant-progress-bg {
    background-color: rgb(255, 255, 255);
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

export const ProgressContainer = styled.div`
  display: flex;
  justify-content: center;
  width: 100%;
  margin-left: 30px;
`;

export const ProgressItem = styled.div`
  margin-right: 5px;
  width: 81px;
  display: flex;
  align-items: center;
  cursor: pointer;
`;

export const WrapperMainImage = styled.div`
  display: flex;
  justify-content: center;
`;

export const MainImage = styled.img`
  width: 100%;
  cursor: pointer;
`;

export const StoryHeader = styled.div`
  display: flex;
  margin: 56px 0px 61px 0px;
`;

export const CloseButton = styled.img`
  position: relative;
  right: 44px;
  cursor: pointer;
`;

export const WrapperDescription = styled.div`
  display: flex;
  flex-direction: column;
  margin-top: 28px;
`;

export const DescriptionTitle = styled.span`
  color: rgb(255, 255, 255);
  font-family: SB Sans Interface;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: 0px;
  word-break: break-all;
`;

export const DescriptionText = styled.span`
  color: rgb(255, 255, 255);
  font-family: SB Sans Text;
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  margin-top: 8px;
  white-space: pre-line;
`;

export const WrapperInfoStory = styled.div`
  width: 40%;

  @media screen and (width > 1500px) {
    width: 50%;
  }
`;

export const InformationSection = styled.div<{ type: string }>`
  position: relative;
  font-family: 'SB Sans Text', 'sans-serif';
  font-size: 16px;
  font-weight: 400;
  line-height: 24px;
  letter-spacing: -0.3px;
  color: ${({ type }) => {
    if (type === 'info') {
      return '#9fe5c3';
    } else if (type === 'warning') {
      return '#ffd7ad';
    }
  }};
  padding-left: 32px;
  margin-top: 8px;
  white-space: pre-line;
`;

export const InformationIcon = styled.img`
  position: absolute;
  top: 4px;
  left: 0;
`;
