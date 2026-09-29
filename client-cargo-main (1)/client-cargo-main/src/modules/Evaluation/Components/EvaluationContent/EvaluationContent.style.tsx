/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const IconBlockRequest = styled('div')<any>`
  margin-bottom: ${props => (props.comment ? '32px' : '96px')};
  display: flex;
  justify-content: center;

  & svg {
    margin-left: 24px;
    cursor: ${props => (props.rating ? 'not-allowed' : 'pointer')};
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

export const RequestContent = styled('div')<any>`
  text-align: center;
`;

export const Title = styled('p')<any>`
  margin-top: 40px;
`;

export const Subtitle = styled('p')<any>`
  text-align: center;
`;

export const TextAreaBlock = styled('div')<any>`
  margin: 32px 0;
`;
