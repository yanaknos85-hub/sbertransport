import styled from 'styled-components';

export const AuthorContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
`;

export const AuthorRow = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-items: start;
`;

export const AuthorInfo = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: flex-start;
`;

export const AuthorValue = styled.span`
  font-size: 16px;
  color: var(--black-text);
`;

export const AuthorPhoneValue = styled.span`
  color: var(--gray9);
`;

export const NoteInfo = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: flex-start;
`;

export const NoteLabel = styled.span`
  font-size: 16px;
  color: var(--black-text);
`;

export const NoteValue = styled.span`
  font-size: 14px;
  color: var(--gray9);
`;
