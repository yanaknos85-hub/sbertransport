import styled from 'styled-components';
import { BarProps, OrientationKind } from './Types';

const StyledBar = styled.div<BarProps>`
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: ${props => (props.orientation === OrientationKind.Vertical ? 'column' : 'row')};
`;

export default StyledBar;
