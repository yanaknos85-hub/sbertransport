/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const CargoType = styled('div')<any>`
  display: flex;
  align-items: center;
  padding: 1px 8px;
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid #c2c2c2;
`;

export const RateBlock = styled('div')<any>`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 32px;

  p {
    margin-bottom: 4px;
  }
`;

export const ButtonBlock = styled('div')<any>`
  text-align: right;
  margin-top: 32px;
`;

export const CommentBlock = styled('div')<any>`
  margin-top: 32px;
`;

export const IconBlock = styled('div')<any>`
  display: flex;
  justify-content: center;
  margin-bottom: 32px;

  & svg {
    margin-left: 24px;
  }

  & svg:nth-child(1) {
    margin-left: 0;
  }

  & div {
    margin-left: 24px;
  }

  & div:nth-child(1) {
    margin-left: 0;
  }
`;

export const SuccessBlock = styled('div')<any>`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 220px;
`;

export const FeedbackRequest = styled('div')<any>`
  opacity: 0.6;
`;
export const RequestContent = styled('div')<any>`
  text-align: center;
`;

export const Title = styled('p')<any>`
  margin-top: 40px;
`;

export const Subtitle = styled('p')<any>`
  text-align: center;
`;
