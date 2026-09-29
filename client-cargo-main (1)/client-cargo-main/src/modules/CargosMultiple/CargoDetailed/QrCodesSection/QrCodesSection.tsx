import React from 'react';

import * as S from './QrCodesSection.style';

export interface QrCodesSectionProps {
  qrs?: string[];
}

export const QrCodesSection: React.FC<QrCodesSectionProps> = ({ qrs }) => {
  if (!qrs || qrs.length === 0) {
    return null;
  }

  return (
    <S.Wrapper>
      <div style={{ fontWeight: 600, marginBottom: '8px' }}>Отсканированные QR-коды</div>
      {qrs.map((code, index) => (
        <S.Item key={index}>{code}</S.Item>
      ))}
    </S.Wrapper>
  );
};
