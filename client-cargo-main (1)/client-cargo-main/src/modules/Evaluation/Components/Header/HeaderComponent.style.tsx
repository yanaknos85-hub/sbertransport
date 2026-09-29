import styled from 'styled-components';

export const Header = styled('div')<any>`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;

  .title {
    font-family: 'SB Sans Interface SemiBold', serif, sans-serif;
    font-size: 20px;
    color: #262626;
  }

  .description {
    font-size: 16px;
    color: #909090;
  }
`;
