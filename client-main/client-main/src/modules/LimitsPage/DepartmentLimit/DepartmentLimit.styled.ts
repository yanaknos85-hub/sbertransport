import styled from 'styled-components';

export const Container = styled('div')`
  background-color: var(--pure-white);
  width: 100%;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;

  @media (max-width: 800px) {
    width: 100%;
    flex-wrap: wrap;
  }
`;
