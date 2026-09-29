import styled from 'styled-components';
import { ReactComponent as UpArrowSVG } from '../../../images/upArrowIcon.svg';
import { ReactComponent as DownArrowSVG } from '../../../images/downArrowIcon.svg';

export const Wrapper = styled.div`
  display: flex;
  justify-content: space-between;
  flex-direction: column;
  margin: 0 4px 0 5px;
`;

export const UpArrow = styled(UpArrowSVG)`
  cursor: pointer;
  margin-bottom: 24px;
`;

export const DownArrow = styled(DownArrowSVG)`
  cursor: pointer;
`;
