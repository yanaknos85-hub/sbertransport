import styled from 'styled-components';

export const ButtonArrowRight = styled.button`
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: #fff;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
  right: 10px;
  z-index: 2;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  box-shadow: 0px 0px 3px 0px rgba(0, 0, 0, 0.12);
`;

export const ButtonArrowLeft = styled.button`
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: #fff;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
  left: 10px;
  z-index: 2;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0px 0px 3px 0px rgba(0, 0, 0, 0.12);
`;

export const CarouselElevent = styled.div`
  overflow: hidden;
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  padding: 0 0 0 8px;
`;

export const CarouselItems = styled.div<{ direction? }>`
  display: flex;
  transition: transform 0.5s ease;
  width: 100%;
  height: 100%;

  flex-direction: ${({ direction }) => (direction ? direction : 'row')};
  gap: ${({ direction }) => (direction === 'column' ? '8px' : 0)};
`;
