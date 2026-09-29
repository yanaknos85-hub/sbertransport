import styled from 'styled-components';

export const FormWrapper = styled.div`
  margin-top: 24px;
  width: 100%;
`;

export const InputWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  flex-direction: row;
  margin-top: 12px;
  width: 100%;

  @media (max-width: 1200px) {
    flex-direction: column;
  }
`;

export const FieldWrapper = styled.div`
  width: 49%;

  @media (max-width: 1200px) {
    width: 100%;
  }
`;
