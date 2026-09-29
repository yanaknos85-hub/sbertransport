import styled from 'styled-components';

export const EmptyDataListStyled = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0 60px;
  font-family: SB Sans Text;

  h3 {
    font-weight: 600;
  }

  img {
    max-width: 280px;
  }

  i {
    color: var(--gray7);
    font-size: 16px;
    font-style: inherit;
    font-weight: 400;
  }

  @media (max-width: 500px) {
    h3 {
      font-size: 14px;
    }

    img {
      max-width: 180px;
    }
  }
`;
