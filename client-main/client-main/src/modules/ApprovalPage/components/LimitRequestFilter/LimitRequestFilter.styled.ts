import styled from 'styled-components';

export const FilterContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 50px;

  .ant-picker-focused {
    box-shadow: none;

    &::before {
      background-color: var(--jade);
    }
  }
`;

export const TransportList = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
`;

export const TransportItem = styled.div<{ isActive: boolean }>`
  display: flex;
  padding: 6px 16px;
  border-radius: 18px;
  background-color: ${({ isActive }) => (isActive ? '#3A424A' : '#dfe2e4')};
  cursor: pointer;

  > span {
    font-family: 'SB Sans Text';
    font-weight: 400;
    color: ${({ isActive }) => (isActive ? 'var(--pure-white)' : 'var(--black-text)')};
    letter-spacing: -0.3px;
    white-space: nowrap;
  }
`;

export const StyledIcon = styled.div<{ src: string }>`
  display: flex;
  width: 24px;
  height: 24px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
  background-position: center;
  margin-right: 10px;
`;

export const FilterContent = styled.div`
  display: none;

  @media (max-width: 500px) {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-grow: 3;
    margin-right: 8px;

    svg path {
      fill: var(--black-text);
    }
  }
`;
