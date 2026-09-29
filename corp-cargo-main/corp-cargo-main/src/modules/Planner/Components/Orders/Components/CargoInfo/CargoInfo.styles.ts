import styled from "styled-components";
import { colors, fontFamily } from "shared/styles/styles";

export const CargoInfo = styled.div`
  display: flex;
  justify-content: space-between;
  flex: 1 0 0;
`;

export const CargoParams = styled.div`
  text-align: center;
  svg {
    width: 16px;
    height: 16px;
  }

  p {
    margin: 0;
    padding: 0;
  }

  p:nth-child(2) {
    font-size: 12px;
    font-family: ${fontFamily.SBSansInterface};
    font-weight: 400;
    color: ${colors.gray10};
  }

  p:nth-child(3) {
    font-size: 14px;
    font-family: ${fontFamily.SBSansTextRegular};
    color: ${colors.gray7};
  }
`;
