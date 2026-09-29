import styled from 'styled-components';

export const ListWrapper = styled.div`
  display: flex;
  flex-direction: column;
  overflow: auto;
  height: 100%;
  min-height: 1px;
  overflow-y: scroll;
  padding: 20px 24px 0 24px;
  flex: 1;
  width: 100%;

  @media (max-height: 768px) {
    height: 360px;
  }
`;
