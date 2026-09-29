import styled from 'styled-components';

export const PaginationContainer = styled.div`
  display: flex;
  justify-content: center;
  padding-top: var(--padding-half);
  margin-top: 12px;
`;

export const HeaderContainer = styled.div`
  display: flex;
  justify-content: space-between;

  @media (max-width: 767px) {
    flex-direction: column;
    margin-left: 10px;
  }
`;

export const MenuContainer = styled.div`
  display: flex;
  flex-direction: column;
`;

export const ListWrapper = styled.div<{ $isMobile?: boolean }>`
  width: 100%;
  overflow-y: auto;
  height: 75vh;

  @media (max-width: 767px) {
    height: ${props => props.$isMobile ? 'calc(100vh - 245px)' : '75vh'};
  }
`;
