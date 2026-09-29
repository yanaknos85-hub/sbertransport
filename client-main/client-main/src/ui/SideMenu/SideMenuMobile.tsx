import styled from 'styled-components';
import SideMenu from './SideMenu';
import { AvatarWrapper, ProfileWrapper } from './components/profile/profile.styled';
import { BottomMenu } from './styled';

export const SideMenuMobile = styled(SideMenu)`
  width: 100%;

  ${ProfileWrapper} {
    display: none;
  }

  ${AvatarWrapper} {
    display: none;
  }

  ${BottomMenu} {
    display: none;
  }
`;
