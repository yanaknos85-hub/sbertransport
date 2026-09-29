import styled from 'styled-components';

export const CargoLayoutWrapper = styled.div`
  width: 100%;

  @media (min-width: 1260px) {
    display: flex;
    justify-content: center;
  }
`;
export const CargoLayout = styled.div`
  position: relative;
  max-width: 1156px;
  width: 100%;

  ::-webkit-scrollbar {
    display: none;
  }
`;

export const CargoContainer = styled.div`
  display: flex;
`;

export const CargoMainContent = styled.div`
  width: calc(100% - 368px - 16px);

  @media (max-width: 767px) {
    width: 100%;
  }
`;

export const CargoMainContentFull = styled.div`
  width: calc(100% - 16px);

  @media (max-width: 767px) {
    width: 100%;
  }
`;

export const CargoMainInnerContent = styled.div`
  background: var(--pure-white);
  border-radius: 8px;
  box-shadow: 0 0 1px rgba(0, 0, 0, 0.12), 0 3px 10px -4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 16px;
`;

export const CargoSideContent = styled.div`
  margin-left: 16px;
`;

export const CargoSideInnerContent = styled.div`
  background-color: var(--pure-white);
  width: 368px;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  /* flex: 1 0 0; */
    @media (max-width: 767px) {
      width: 100%;
    }
`;

export const CargoFixed = styled.div`
  //position: fixed;
`;

export default CargoLayout;
