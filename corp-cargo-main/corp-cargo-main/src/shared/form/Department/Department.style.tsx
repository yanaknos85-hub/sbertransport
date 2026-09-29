import styled from 'styled-components';

import { autocomplete, borderHoverDisabled, borderHover } from '../styles';

export const StyledDepartment = styled('div')<{ disabled: boolean }>`
  ${autocomplete}

  .ant-select-selection-search-input {
    padding: 13px 16px 13px 11px;
    cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
    // Удалить, как только будет готов бэк
    // Эта странная конструкция нужна потому что я не знаю, как перебить стиль выше
    & :hover {
      border: ${({ disabled }) => (disabled ? borderHoverDisabled : borderHover)}
    }
  }
`;
