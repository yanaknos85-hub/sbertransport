import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const ListWrapper = styled.div`
  display: flex;
  flex-direction: column;
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 1px;
  overflow: auto;
  padding: 20px 24px 0 24px;
  position: relative;
  background-color: ${colors.white};

  ::-webkit-scrollbar {
    display: none;
  }

  @media (max-height: 768px) {
    height: 374px;
  }
`;
