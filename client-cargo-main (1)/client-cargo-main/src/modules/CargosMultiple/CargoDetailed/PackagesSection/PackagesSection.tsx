import React from 'react';

import * as S from './PackagesSection.style';

export interface PackagesSectionItem {
  id: string;
  name: string;
  count: number;
}

export interface PackagesSectionProps {
  packages?: PackagesSectionItem[];
}

export const PackagesSection: React.FC<PackagesSectionProps> = ({ packages }) => {
  if (!packages || packages.length === 0) {
    return null;
  }

  return (
    <S.Wrapper>
      {packages.map(item => (
        <S.Item key={item.id}>
          {item.name}
          {' '}
          x
          {item.count}
        </S.Item>
      ))}
    </S.Wrapper>
  );
};
