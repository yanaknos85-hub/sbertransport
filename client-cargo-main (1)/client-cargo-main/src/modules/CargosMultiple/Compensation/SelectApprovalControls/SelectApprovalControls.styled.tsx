import TButton from 'shared/ui/Button/Button';
import styled from 'styled-components';

export const Wrapper = styled.div<{ $isChecked: boolean }>`
  display: flex;
  justify-content: flex-end;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: #F7F8FA;
  padding-bottom: 4px;
  border-radius: 8px;

  @media(max-width: 767px) {
    padding: 0 10px 10px;
    margin: 0 auto;
    width: 97%;
    height: ${props => (props.$isChecked ? '75px' : '20px')};
  }
`;

export const Container = styled.div`
  background-color: white;
  border-radius: 8px;
  margin-left: 75px;
  padding: 4px 30px;
  margin-right: 8px;
  color: rgb(115, 115, 11);
  width: 200px;
  display: flex;
  justify-content: space-between;

  @media(max-width: 767px) {
    position: absolute;
    right: 0;
    top: 0;
    margin-left: 0;
    margin-right: 0;
    padding: 4px 10px;
    width: 150px;
  }
`;

export const ButtonsContainer = styled.div`
  display: flex;

  @media(max-width: 767px) {
    flex-direction: column;
    justify-content: space-between;
    position: absolute;
    left: 0;
    top: 0;
  }
`;

export const Button = styled(TButton)`
  padding: 0 20px;
  height: 30px;
  font-size: 14px;
  font-weight: 400;

  &:nth-child(1) {
    margin-right: 12px;

    @media(max-width: 767px) {
      margin-bottom: 12px;
    }
  }

  @media(max-width: 767px) {
    padding: 0 10px;
  }
`;

export const DeclineButton = styled(Button)`
  background: inherit;
  color: black;
  border: none;
  box-shadow: none;
`;
